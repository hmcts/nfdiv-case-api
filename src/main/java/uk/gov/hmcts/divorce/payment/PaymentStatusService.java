package uk.gov.hmcts.divorce.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.payment.model.Payment;

@Service
public class PaymentStatusService {

    private static final String SUCCESS = "Success";
    @Autowired
    private PaymentClient paymentClient;

    public boolean paymentSuccessful(String authorisation, String serviceAuthorisation, String paymentReference) {

        final Payment payment = paymentClient.getPaymentByReference(
            authorisation,
            serviceAuthorisation,
            paymentReference
        );

        return SUCCESS.equals(payment.getStatus());
    }
}
