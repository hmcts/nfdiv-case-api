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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRespondentFOPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class RespondentFinalOrderPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String RESPONDENT_FINAL_ORDER_PAYMENT_MADE = "respondent-final-order-payment-made";

    private final PaymentValidatorService paymentValidatorService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(RESPONDENT_FINAL_ORDER_PAYMENT_MADE)
            .forState(AwaitingRespondentFOPayment)
            .showCondition(NEVER_SHOW)
            .name("Respondent final order payment made")
            .description("Respondent final order payment made")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, APPLICANT_2)
            .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR, APPLICANT_2_SOLICITOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        Long caseId = details.getId();

        log.info("{} about to submit callback invoked CaseID: {}", RESPONDENT_FINAL_ORDER_PAYMENT_MADE, caseId);

        List<String> validationErrors = paymentValidatorService.validatePayments(
            caseData.getFinalOrder().getFinalOrderPayments(), caseId
        );

        if (CollectionUtils.isNotEmpty(validationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(validationErrors)
                .state(AwaitingRespondentFOPayment)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(RespondentFinalOrderRequested)
            .build();
    }
}

