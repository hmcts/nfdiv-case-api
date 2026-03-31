package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0228;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralApplicationPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateAosSubmitted;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERAL_APPLICATION = "citizen-general-application";

    public static final String AWAITING_PAYMENT_ERROR = """
        A general application has already been submitted and is awaiting payment.
        """;

    private final PaymentSetupService paymentSetupService;

    private final Clock clock;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    private final GeneralReferralService generalReferralService;

    private final CitizenGeneralApplicationSubmissionService submissionService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_GENERAL_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Citizen general application")
            .description("Citizen general application")
            .showSummary()
            .showEventNotes()
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        long caseId = details.getId();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION, caseId);

        boolean isApplicant1 = isApplicant1(details.getId());
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        if (applicant.getGeneralAppServiceRequest() != null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(AWAITING_PAYMENT_ERROR))
                .build();
        }

        InterimApplicationOptions userOptions = applicant.getInterimApplicationOptions();
        GeneralApplicationType generalApplicationType = userOptions.getGeneralApplicationType();
        boolean isSearchGovernmentRecords = GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(
            userOptions.getGeneralApplicationType());

        if (isSearchGovernmentRecords) {
            List<String> errors = validateAosSubmitted(data);
            if (!errors.isEmpty()) {
                log.info("{} failed since partner has already responded for {} ", CITIZEN_GENERAL_APPLICATION, caseId);
                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .errors(errors)
                    .build();
            }
        }

        GeneralApplication newGeneralApplication = buildGeneralApplication(
            userOptions, GeneralParties.from(isApplicant1, data.getApplicationType())
        );

        FeeDetails applicationFee = newGeneralApplication.getGeneralApplicationFee();
        if (userOptions.willMakePayment()) {
            newGeneralApplication.setGeneralApplicationFeeType(userOptions.isHearingRequired() ? FEE0227 : FEE0228);

            String serviceRequest = prepareGeneralApplicationForPayment(newGeneralApplication, applicant, caseId);
            applicant.setActiveGeneralApplication(serviceRequest);

            if (GeneralApplicationType.DISCLOSURE_VIA_DWP.equals(generalApplicationType)) {
                details.setState(AwaitingGeneralApplicationPayment);
            }
        } else {
            applicationFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);
            applicationFee.setHelpWithFeesReferenceNumber(userOptions.getInterimAppsHwfRefNumber());

            if (submissionService.canBeAutoReferred(data, generalApplicationType)) {
                GeneralReferral automaticReferral = generalReferralService.buildGeneralReferral(newGeneralApplication);
                data.setGeneralReferral(automaticReferral);
            }

            submissionService.setEndState(details, newGeneralApplication);
        }

        DivorceDocument applicationDocument = submissionService.generateGeneralApplicationAnswerDocument(
            caseId, applicant, data, newGeneralApplication
        );
        newGeneralApplication.setGeneralApplicationDocument(applicationDocument);

        data.updateCaseWithGeneralApplication(newGeneralApplication);
        applicant.archiveInterimApplicationOptions();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION, details.getId());

        var beforeGeneralApplications = new ArrayList<>(
            Optional.ofNullable(beforeDetails.getData().getGeneralApplications())
                .orElseGet(Collections::emptyList)
        );
        var afterGeneralApplications = new ArrayList<>(
            Optional.ofNullable(details.getData().getGeneralApplications())
                .orElseGet(Collections::emptyList)
        );

        afterGeneralApplications.removeAll(beforeGeneralApplications);

        GeneralApplication newGeneralApplication = afterGeneralApplications.getLast().getValue();
        ServicePaymentMethod paymentMethod = newGeneralApplication.getGeneralApplicationFee().getPaymentMethod();

        if (ServicePaymentMethod.FEE_PAY_BY_HWF.equals(paymentMethod)) {
            submissionService.sendNotifications(
                details.getId(), newGeneralApplication, details.getData()
            );
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private GeneralApplication buildGeneralApplication(InterimApplicationOptions userOptions, GeneralParties generalParty) {
        return GeneralApplication.builder()
            .generalApplicationParty(generalParty)
            .generalApplicationReceivedDate(LocalDateTime.now(clock))
            .generalApplicationType(userOptions.getGeneralApplicationType())
            .generalApplicationSubmittedOnline(YesOrNo.YES)
            .generalApplicationDocuments(submissionService.collectSupportingDocuments(userOptions))
            .generalApplicationDocsUploadedPreSubmission(userOptions.isAwaitingDocuments() ? YesOrNo.NO : YesOrNo.YES)
            .build();
    }

    private String prepareGeneralApplicationForPayment(GeneralApplication generalApplication, Applicant applicant, long caseId) {
        FeeDetails applicationFee = generalApplication.getGeneralApplicationFee();
        applicationFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD);
        applicationFee.setHasCompletedOnlinePayment(YesOrNo.NO);

        OrderSummary orderSummary = paymentSetupService.createGeneralApplicationOrderSummary(
            caseId, generalApplication.getGeneralApplicationFeeType()
        );
        String serviceRequest = paymentSetupService.createGeneralApplicationPaymentServiceRequest(
            orderSummary, caseId, applicant.getFullName()
        );

        FeeDetails serviceFee = generalApplication.getGeneralApplicationFee();
        serviceFee.setOrderSummary(orderSummary);
        serviceFee.setServiceRequestReference(serviceRequest);

        return serviceRequest;
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }
}
