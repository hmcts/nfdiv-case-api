package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.payment.PaymentService;

import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEEMED;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Service
@Slf4j
public class AlternativeServicePaymentService {

    @Autowired
    private PaymentService paymentService;

    public CaseData getFeeAndSetOrderSummary(final CaseData caseData, final Long caseId) {
        log.info("Retrieve the Alternative Service fee and set the OrderSummary for Case Id: {}", caseId);

        OrderSummary orderSummary;

        if (caseData.getAlternativeService().getAlternativeServiceType() == AlternativeServiceType.BAILIFF) {
            orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF);
        } else {
            orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_DEEMED);
        }
        caseData.getAlternativeService().getServicePaymentFee().setOrderSummary(orderSummary);

        for (ListValue<Fee> entry : orderSummary.getFees()) {
            Fee fee = entry.getValue();
            log.info("orderSummary code {} description {} value {}", fee.getAmount(), fee.getDescription(), fee.getCode());
        }

        return caseData;
    }


}
