package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralReason;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralType;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenGeneralApplicationPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERAL_APPLICATION_PAYMENT = "citizen-gen-app-payment-made";

    private final Clock clock;

    private final PaymentValidatorService paymentValidatorService;

    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    private static final String GENERAL_APPLICATION_NOT_FOUND = "No general application found that is awaiting payment";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_GENERAL_APPLICATION_PAYMENT)
            .forState(GeneralApplicationReceived)
            .name("Citizen gen app payment made")
            .description("Citizen gen app payment made")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, APPLICANT_1_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        long caseId = details.getId();
        CaseData data = details.getData();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION_PAYMENT, details.getId());

        boolean isApplicant1 = isApplicant1(caseId);
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        Optional<GeneralApplication> generalAppOptional = findActiveGeneralApplication(data, applicant);

        if (generalAppOptional.isEmpty()) {
            log.info(
                "Failed to find active general application to record payment against for {} on Case Id: {}",
                isApplicant1 ? "Applicant 1" : "Respondent", caseId
            );

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(GENERAL_APPLICATION_NOT_FOUND))
                .build();
        }

        GeneralApplication generalApplication = generalAppOptional.get();

        List<ListValue<Payment>> payments = applicant.getGenApplicationPayments();
        List<String> validationErrors = paymentValidatorService.validatePayments(payments, caseId);
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .errors(validationErrors)
                    .build();
        }

        savePaymentDetails(payments, generalApplication, applicant);

        boolean isAwaitingDocuments = YesOrNo.NO.equals(generalApplication.getGeneralApplicationDocsUploadedPreSubmission());
        boolean notReadyForAutomaticReferral = isGeneralReferralInProgress(data.getGeneralReferral()) || isAwaitingDocuments;
        if (notReadyForAutomaticReferral) {
            details.setState(isAwaitingDocuments ? AwaitingDocuments : GeneralApplicationReceived);
        } else {
            data.setGeneralReferral(buildGeneralReferral(generalApplication, isApplicant1));
            details.setState(AwaitingGeneralConsideration);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION_PAYMENT, details.getId());

        CaseData beforeData = beforeDetails.getData();
        CaseData data = details.getData();

        boolean isApplicant1 = isApplicant1(details.getId());
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        Optional<GeneralApplication> generalAppOptional = findActiveGeneralApplication(beforeData, applicant);

        generalAppOptional.ifPresent(generalApplication ->
            interimApplicationSubmissionService.sendGeneralApplicationNotifications(
                details.getId(), generalAppOptional.get(), data
            ));

        return SubmittedCallbackResponse.builder().build();
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private Optional<GeneralApplication> findActiveGeneralApplication(CaseData caseData, Applicant applicant) {
        if (CollectionUtils.isEmpty(caseData.getGeneralApplications())) {
            return Optional.empty();
        }

        String serviceRequest = applicant.getGeneralApplicationServiceRequest();

        return caseData.getGeneralApplications().stream()
            .map(ListValue::getValue)
            .filter(generalApplication ->
                isActiveGeneralApplication(generalApplication, serviceRequest)
            ).findFirst();
    }

    private boolean isActiveGeneralApplication(
        GeneralApplication generalApplication, String expectedServiceRequest
    ) {
        FeeDetails applicationFee = generalApplication.getGeneralApplicationFee();

        return applicationFee != null && applicationFee.getServiceRequestReference() != null
            && expectedServiceRequest.equals(applicationFee.getServiceRequestReference());
    }

    private void savePaymentDetails(
        List<ListValue<Payment>> payments,
        GeneralApplication generalApplication,
        Applicant applicant)
    {
        String paymentReference = paymentValidatorService.getLastPayment(payments).getReference();
        generalApplication.getGeneralApplicationFee().setPaymentReference(paymentReference);
        generalApplication.getGeneralApplicationFee().setDateOfPayment(LocalDate.now(clock));
        applicant.setGenApplicationPayments(null);
        applicant.setGeneralApplicationServiceRequest(null);
        applicant.setGeneralApplicationOrderSummary(null);
    }

    private boolean isGeneralReferralInProgress(GeneralReferral generalReferral) {
        return generalReferral != null && generalReferral.getGeneralReferralType() != null;
    }

    private GeneralReferral buildGeneralReferral(GeneralApplication generalApplication, boolean isApplicant1) {
        return GeneralReferral.builder()
            .generalReferralReason(GeneralReferralReason.GENERAL_APPLICATION_REFERRAL)
            .generalApplicationFrom(isApplicant1 ? GeneralParties.APPLICANT : GeneralParties.RESPONDENT)
            .generalApplicationReferralDate(LocalDate.now(clock))
            .generalApplicationAddedDate(generalApplication.getReceivedGeneralApplicationDate())
            .generalReferralType(GeneralReferralType.DISCLOSURE_VIA_DWP)
            .generalReferralFee(generalApplication.getGeneralApplicationFee())
            .build();
    }
}
