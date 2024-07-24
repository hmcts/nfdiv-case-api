package uk.gov.hmcts.divorce.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentRequest;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "fees-and-payments-client", url = "${payment.service.api.baseurl}")
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface PaymentPbaClient {

    @PostMapping(value = "/credit-account-payments")
    ResponseEntity<CreditAccountPaymentResponse> creditAccountPayment(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        CreditAccountPaymentRequest creditAccountPaymentRequest);

}
