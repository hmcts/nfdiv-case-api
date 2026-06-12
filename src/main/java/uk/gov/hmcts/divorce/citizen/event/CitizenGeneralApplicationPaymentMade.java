package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LABEL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_LABEL;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenGeneralApplicationPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERAL_APPLICATION_PAYMENT = "citizen-general-app-payment";

    private final Clock clock;

    private final PaymentValidatorService paymentValidatorService;

    private final CitizenGeneralApplicationSubmissionService submissionService;

    private final GeneralReferralService generalReferralService;

    private static final String GENERAL_APPLICATION_NOT_FOUND = "No general applications are awaiting payment";
    private static final String ERROR_UNABLE_TO_FIND_PAYMENT_PARTY = "Unable to find general application payment party";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_GENERAL_APPLICATION_PAYMENT)
            .forStates(POST_SUBMISSION_STATES)
            .name("General application payment")
            .description("General application payment made")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_2, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        long caseId = details.getId();
        CaseData data = details.getData();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION_PAYMENT, caseId);

        final boolean isApplicant1 = paymentMadeByApplicant1(details.getData(), beforeDetails.getData());
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        log.info(
            "Processing Citizen General Application payment for {}, Case Id: {}",
            isApplicant1 ? APPLICANT_LABEL : APPLICANT_2_LABEL, caseId
        );

        Optional<GeneralApplication> generalAppOptional = submissionService.findActiveGeneralApplication(data, applicant);

        if (generalAppOptional.isEmpty()) {
            log.info("Failed to find active general application for payment, party: {}, case id: {}",
                isApplicant1 ? "Applicant 1" : "Respondent/Applicant2", caseId);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(GENERAL_APPLICATION_NOT_FOUND))
                .build();
        }

        GeneralApplication generalApplication = generalAppOptional.get();
        List<ListValue<Payment>> payments = applicant.getGeneralAppPayments();

        List<String> validationErrors = paymentValidatorService.validatePayments(payments, caseId);
        if (CollectionUtils.isNotEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .errors(validationErrors)
                    .build();
        }

        String paymentReference = paymentValidatorService.getLastPayment(payments).getReference();
        generalApplication.recordPayment(paymentReference, LocalDate.now(clock));
        applicant.setActiveGeneralApplication(null);

        if (submissionService.canBeAutoReferred(data, generalApplication.getGeneralApplicationType())) {
            GeneralReferral automaticReferral = generalReferralService.buildGeneralReferral(generalApplication);
            data.setGeneralReferral(automaticReferral);
            generalApplication.setGeneralApplicationReferralDate(data.getGeneralReferral().getGeneralApplicationReferralDate());
        }

        submissionService.setEndState(details, generalApplication);

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

        final Applicant beforeApplicant2 = beforeData.getApplicant2();
        final Applicant beforeApplicant1 = beforeData.getApplicant1();
        final Applicant applicant1 = data.getApplicant1();

        final boolean isApplicant1 = beforeApplicant1.hasUnpaidGeneralApplication() && !applicant1.hasUnpaidGeneralApplication();

        Applicant beforeApplicant = isApplicant1 ? beforeApplicant1 : beforeApplicant2;
        Optional<GeneralApplication> generalAppOptional = submissionService.findActiveGeneralApplication(data, beforeApplicant);

        data.getApplication().setPreviousState(beforeDetails.getState());

        generalAppOptional.ifPresent(generalApplication ->
            submissionService.sendNotifications(details.getId(), generalAppOptional.get(), data)
        );

        return SubmittedCallbackResponse.builder().build();
    }

    private boolean paymentMadeByApplicant1(CaseData caseData, CaseData beforeData) {
        final boolean applicant1HasPaid = hasMadeGeneralAppPayment(caseData.getApplicant1(), beforeData.getApplicant1());
        final boolean applicant2HasPaid = hasMadeGeneralAppPayment(caseData.getApplicant2(), beforeData.getApplicant2());

        if (applicant1HasPaid == applicant2HasPaid) {
            throw new IllegalStateException(ERROR_UNABLE_TO_FIND_PAYMENT_PARTY);
        }

        return applicant1HasPaid;
    }

    private boolean hasMadeGeneralAppPayment(Applicant applicant, Applicant beforeApplicant) {
        final int successfulPaymentCount = countSuccessfulPayments(applicant.getGeneralAppPayments());
        final int beforeSuccessfulPaymentCount = countSuccessfulPayments(beforeApplicant.getGeneralAppPayments());

        return applicant.getGeneralAppServiceRequest() != null && successfulPaymentCount > beforeSuccessfulPaymentCount;
    }

    private int countSuccessfulPayments(List<ListValue<Payment>> payments) {
        if (CollectionUtils.isEmpty(payments)) {
            return 0;
        }

        return (int) payments.stream()
            .map(ListValue::getValue)
            .filter(payment -> PaymentStatus.SUCCESS.equals(payment.getStatus()))
            .count();
    }
}
