package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.PaymentsResponse;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Applicant2Approved;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Archived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateReadyForPayment;

@Component
@Slf4j
public class SystemSolicitorPayment implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private PaymentService paymentService;

    public static final String SYSTEM_SOLICITOR_PAYMENT = "system-solicitor-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SYSTEM_SOLICITOR_PAYMENT)
            .forStates(Draft, Applicant2Approved, Archived)
            .name("Solicitor PBA payment")
            .description("Invoke solicitor PBA payment")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
        // I dont think we need retries here
        // .retries(120, 120));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SYSTEM_SOLICITOR_PAYMENT, details.getId());
        final Long caseId = details.getId();
        final var caseData = details.getData();

        log.info("Validating case data CaseID: {}", caseId);
        final List<String> submittedErrors = validateReadyForPayment(caseData);

        if (!submittedErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .errors(submittedErrors)
                .build();
        }

        final var application = caseData.getApplication();
        final var applicationFeeOrderSummary = application.getApplicationFeeOrderSummary();

        if (caseData.getApplication().isSolicitorPaymentMethodPba()) {
            final Optional<String> pbaNumber = application.getPbaNumber();
            if (pbaNumber.isPresent()) {
                if (alreadyHasSuccessfulPaymentForCase(caseId)) {
                    return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                        .data(details.getData())
                        .errors(singletonList("already has payment on case: " + caseId))
                        .build();
                }
                final PbaResponse response = paymentService.processPbaPayment(
                    caseData,
                    caseId,
                    caseData.getApplicant1().getSolicitor(),
                    pbaNumber.get(),
                    caseData.getApplication().getApplicationFeeOrderSummary(),
                    caseData.getApplication().getFeeAccountReference()
                );

                if (response.getHttpStatus() == CREATED) {
                    caseData.updateCaseDataWithPaymentDetails(applicationFeeOrderSummary, caseData, response.getPaymentReference());
                } else {
                    return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                        .data(details.getData())
                        .errors(singletonList(response.getErrorMessage()))
                        .build();
                }
            } else {
                log.error(
                    "PBA number not present when payment method is 'Solicitor fee account (PBA)' for CaseId: {}",
                    details.getId());

                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(details.getData())
                    .errors(singletonList("PBA number not present when payment method is 'Solicitor fee account (PBA)'"))
                    .build();
            }
        }

        updateApplicant2DigitalDetails(caseData);

        final CaseDetails<CaseData, State> updatedCaseDetails = submissionService.submitApplication(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedCaseDetails.getData())
            .state(updatedCaseDetails.getState())
            .build();
    }

    private boolean alreadyHasSuccessfulPaymentForCase(Long caseId) {
        PaymentsResponse paymentsResponse = paymentService.getPaymentsOnCase(caseId.toString());
        long numberSuccessful = paymentsResponse.getPayments().stream().filter(
            paymentResponse -> "Success".equals(paymentResponse.getStatus())).count();

        return numberSuccessful > 0;
    }

    private void updateApplicant2DigitalDetails(CaseData caseData) {
        if (caseData.getApplicant2().getSolicitor() != null
            && caseData.getApplicant2().getSolicitor().getOrganisationPolicy() != null) {
            log.info("Applicant 2 has a solicitor and is digital");

            caseData.getApplication().setApp2ContactMethodIsDigital(YES);
        }
    }
}
