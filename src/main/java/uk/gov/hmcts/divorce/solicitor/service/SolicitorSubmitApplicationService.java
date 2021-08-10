package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.payment.model.Payment;

import static java.lang.Integer.parseInt;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;

@Service
@Slf4j
public class SolicitorSubmitApplicationService {

    public Payment getDummyPayment(final OrderSummary orderSummary) {
        return Payment
            .builder()
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode("FEE0001")
            .reference(orderSummary.getPaymentReference())
            .status(SUCCESS)
            .transactionId("ge7po9h5bhbtbd466424src9tk")
            .build();
    }
}
