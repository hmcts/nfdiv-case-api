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
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class CaseworkerConfirmServicePayment implements CCDConfig<CaseData, State, UserRole> {

    private static final String KEYWORD_DEEMED = "GeneralAppWithoutNotice";
    private static final String EVENT_GENERAL = "general%20application";
    //private static final String KEYWORD_BAILIFF = "financial-order";

    @Autowired
    private PaymentService paymentService;

    public static final String CASEWORKER_SERVICE_PAYMENT = "caseworker-service-payment";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_SERVICE_PAYMENT)
            .forStateTransition(AwaitingServicePayment, AwaitingServiceConsideration)
            .name("Confirm Service Payment")
            .description("Service Payment made")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, CITIZEN)
            .grant(READ, SUPER_USER, LEGAL_ADVISOR))
            .page("alternativeServicePayment")
            .pageLabel("Payment - service application payment")
            .complex(CaseData::getApplication)
                .complex(Application::getAlternativeService)
                .mandatory(AlternativeService::getPaymentMethod)
                .mandatory(AlternativeService::getFeeAccountNumber, "altServicePaymentMethod = \"feePayByAccount\"")
                .optional(AlternativeService::getFeeAccountReferenceNumber, "altServicePaymentMethod = \"feePayByAccount\"")
                .mandatory(AlternativeService::getHelpWithFeesReferenceNumber, "altServicePaymentMethod = \"feePayByHelp\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("aboutToStart");

        log.info("Retrieving order summary");
        final OrderSummary orderSummary = paymentService.getOrderSummaryByOtherEventKeyword(EVENT_GENERAL, KEYWORD_DEEMED);
        for (ListValue<Fee> entry : orderSummary.getFees()) {
            Fee fee = entry.getValue();
            log.info("orderSummary DEEMED value {}", fee.getAmount());
        }

        final CaseData caseData = details.getData();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(null)
            .warnings(null)
            .build();
    }
}
