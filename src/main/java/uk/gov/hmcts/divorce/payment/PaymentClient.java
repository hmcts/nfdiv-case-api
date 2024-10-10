package uk.gov.hmcts.divorce.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.divorce.payment.model.CreateServiceRequestBody;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.ServiceReferenceResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@FeignClient(name = "payment-client", url = "${payment.service.api.baseurl}")
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface PaymentClient {

    @GetMapping(value = "/card-payments/{paymentReference}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Payment getPaymentByReference(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("paymentReference") String paymentReference);

    @PostMapping(value = "/service-request")
    ResponseEntity<ServiceReferenceResponse> createServiceRequest(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        CreateServiceRequestBody createServiceRequestBody);
}
