package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateSubmission;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_PAYMENT_MADE = "citizen-payment-made";

    private final SubmissionService submissionService;

    private final PaymentValidatorService paymentValidatorService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_PAYMENT_MADE)
            .forState(AwaitingPayment)
            .showCondition(NEVER_SHOW)
            .name("Payment made")
            .description("Payment made")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();

        log.info("Add payment about to submit callback invoked CaseID: {}", caseId);

        List<String> validationErrors = paymentValidatorService.validatePayments(
            caseData.getApplication().getApplicationPayments(), caseId
        );

        if (CollectionUtils.isNotEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .errors(validationErrors)
                .state(AwaitingPayment)
                .build();
        }

        log.info("Validating case caseData CaseID: {}", caseId);
        final List<String> submittedErrors = validateSubmission(caseData.getApplication());

        if (!submittedErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .state(details.getState())
                .errors(submittedErrors)
                .build();
        }

        final CaseDetails<CaseData, State> updatedCaseDetails = submissionService.submitApplication(details);

        if (caseData.getApplicationType().isSole()
            && NO.equals(caseData.getApplication().getApplicant1KnowsApplicant2Address())
            && YES.equals(caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay())) {
            updatedCaseDetails.setState(AwaitingDocuments);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedCaseDetails.getData())
            .state(updatedCaseDetails.getState())
            .build();
    }
}

