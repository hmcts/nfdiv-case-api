package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.notification.RespondentApplyForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.payment.PaymentSetupService;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemNotifyRespondentApplyFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER = "system-notify-respondent-apply-final-order";

    private final NotificationDispatcher notificationDispatcher;

    private final RespondentApplyForFinalOrderNotification respondentApplyForFinalOrderNotification;

    private final PaymentSetupService paymentSetupService;

    @Value("${idam.client.redirect_uri}")
    private String redirectUrl;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_NOTIFY_RESPONDENT_APPLY_FINAL_ORDER)
            .forState(AwaitingFinalOrder)
            .name("Notify respondent final order")
            .description("Notify respondent that they can make a Final Order application")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        final CaseData caseData = details.getData();
        final Long caseId = details.getId();
        log.info("A period of 3 months has elapsed since applicant became eligible to apply for final order for case {}. "
            + "Triggering notificationDispatcher...", caseId);

        notificationDispatcher.send(respondentApplyForFinalOrderNotification, caseData, caseId);

        caseData.getFinalOrder().setFinalOrderReminderSentApplicant2(YES);

        if (caseData.getApplicant2().isRepresented()) {
            prepareCaseDataForFinalOrderPbaPayment(caseData, caseId);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void prepareCaseDataForFinalOrderPbaPayment(CaseData data, long caseId) {
        FinalOrder finalOrder = data.getFinalOrder();

        OrderSummary orderSummary = paymentSetupService.createFinalOrderFeeOrderSummary(data, caseId);

        String serviceRequest = paymentSetupService.createFinalOrderFeeServiceRequest(
            data, caseId, redirectUrl, orderSummary
        );

        finalOrder.setApplicant2FinalOrderFeeOrderSummary(orderSummary);
        finalOrder.setApplicant2SolFinalOrderFeeOrderSummary(orderSummary);
        finalOrder.setApplicant2FinalOrderFeeServiceRequestReference(serviceRequest);
    }
}
