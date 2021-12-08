package uk.gov.hmcts.divorce.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.CHANNEL;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.EVENT;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.JURISDICTION_1;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.JURISDICTION_2;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.KEYWORD;
import static uk.gov.hmcts.divorce.payment.FeesAndPayConstants.SERVICE;

@FeignClient(name = "fees-and-payments-client", url = "${fee.api.baseUrl}")
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface FeesAndPaymentsClient {

    @GetMapping(value = "/fees-register/fees/lookup",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    FeeResponse getPaymentServiceFee(
        @RequestParam(CHANNEL) final String channel,
        @RequestParam(EVENT) final String event,
        @RequestParam(JURISDICTION_1) final String jurisdiction1,
        @RequestParam(JURISDICTION_2) final String jurisdiction2,
        @RequestParam(SERVICE) final String service,
        @RequestParam(KEYWORD) final String keyword
    );
}
