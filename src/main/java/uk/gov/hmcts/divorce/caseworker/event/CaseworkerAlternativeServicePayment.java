package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.caseworker.event.page.AlternativeServicePaymentConfirmation;
import uk.gov.hmcts.divorce.caseworker.event.page.AlternativeServicePaymentSummary;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEEMED;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Slf4j
@Component
public class CaseworkerAlternativeServicePayment implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private PaymentService paymentService;

    public static final String CASEWORKER_SERVICE_PAYMENT = "caseworker-service-payment";

    private final List<CcdPageConfiguration> pages = List.of(
        new AlternativeServicePaymentConfirmation(),
        new AlternativeServicePaymentSummary()
    );

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_SERVICE_PAYMENT)
            .forState(AwaitingServicePayment)
            .name("Confirm service payment")
            .description("Service payment made")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, CITIZEN)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR));
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Retrieve the Alternative Service fee and set the OrderSummary");

        OrderSummary orderSummary;

        final var caseData = details.getData();
        if (caseData.getAlternativeService().getAlternativeServiceType() == AlternativeServiceType.BAILIFF) {
            orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF);
        } else {
            orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_DEEMED);
        }
        caseData.getAlternativeService().setServicePaymentFeeOrderSummary(orderSummary);

        for (ListValue<Fee> entry : orderSummary.getFees()) {
            Fee fee = entry.getValue();
            log.info("orderSummary code {} description {} value {}", fee.getAmount(), fee.getDescription(), fee.getCode());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("CaseWorkerAlternativeServicePayment aboutToSubmit callback invoked");

        final var caseData = details.getData();
        final State state;

        if (caseData.getAlternativeService().getAlternativeServiceType() == AlternativeServiceType.BAILIFF) {
            state = AwaitingBailiffReferral;
        } else {
            state = AwaitingServiceConsideration;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }
}
