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
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
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
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralApplicationPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERAL_APPLICATION = "citizen-general-application";

    public static final String AWAITING_PAYMENT_ERROR = """
        A general application has already been submitted and is awaiting payment.
        """;

    private final PaymentSetupService paymentSetupService;

    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    private final Clock clock;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

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
            .grant(CREATE_READ_UPDATE, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, APPLICANT_1_SOLICITOR));
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
        GeneralApplication newGeneralApplication = buildGeneralApplication(userOptions, isApplicant1);

        FeeDetails applicationFee = newGeneralApplication.getGeneralApplicationFee();
        if (userOptions.willMakePayment()) {
            applicationFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD);
            applicationFee.setHasCompletedOnlinePayment(YesOrNo.NO);
            String serviceRequest = prepareGeneralApplicationForPayment(newGeneralApplication, applicant, caseId);
            applicant.setActiveGeneralApplication(serviceRequest);

            details.setState(AwaitingGeneralApplicationPayment);
        } else {
            applicationFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);
            applicationFee.setHelpWithFeesReferenceNumber(userOptions.getInterimAppsHwfRefNumber());

            details.setState(GeneralApplicationReceived);
        }

        DivorceDocument applicationDocument = interimApplicationSubmissionService
                .generateGeneralApplicationAnswerDocument(caseId, applicant, data, newGeneralApplication);
        newGeneralApplication.setGeneralApplicationDocument(applicationDocument);

        data.updateCaseWithGeneralApplication(newGeneralApplication);

        applicant.archiveInterimApplicationOptions();
        applicant.setInterimApplicationOptions(new InterimApplicationOptions());

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
            interimApplicationSubmissionService.sendGeneralApplicationNotifications(
                details.getId(), newGeneralApplication, details.getData()
            );
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private GeneralApplication buildGeneralApplication(InterimApplicationOptions userOptions, boolean isApplicant1) {
        return GeneralApplication.builder()
            .generalApplicationParty(isApplicant1 ? GeneralParties.APPLICANT : GeneralParties.RESPONDENT)
            .generalApplicationReceivedDate(LocalDateTime.now(clock))
            .generalApplicationType(userOptions.getInterimApplicationType().getGeneralApplicationType())
            .generalApplicationSubmittedOnline(YesOrNo.YES)
            .build();
    }

    private String prepareGeneralApplicationForPayment(GeneralApplication generalApplication, Applicant applicant, long caseId) {
        OrderSummary orderSummary = paymentSetupService.createGeneralApplicationOrderSummary(caseId);
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
