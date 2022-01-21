package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralReferralPaymentConfirmation;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralReferralPaymentSummary;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEEMED;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
public class CaseworkerGeneralReferralPayment implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private PaymentService paymentService;

    public static final String CASEWORKER_GENERAL_REFERRAL_PAYMENT = "caseworker-general-referral-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            new GeneralReferralPaymentConfirmation(),
            new GeneralReferralPaymentSummary()
        );

        var pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_REFERRAL_PAYMENT)
            .forStateTransition(AwaitingGeneralReferralPayment, AwaitingServiceConsideration)
            .name("General referral payment")
            .description("General referral payment")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, LEGAL_ADVISOR)
            .grant(READ, SUPER_USER, SOLICITOR, CITIZEN));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();

        OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_DEEMED);
        caseData.getGeneralReferral().getGeneralReferralFee().setOrderSummary(orderSummary);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
