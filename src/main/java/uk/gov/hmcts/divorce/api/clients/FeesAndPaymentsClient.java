package uk.gov.hmcts.divorce.api.clients;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.divorce.api.constants.FeesAndPayConstants;
import uk.gov.hmcts.divorce.api.model.payments.FeeResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "fees-and-payments-client", url = "${fee.api.baseUrl}")
@SuppressWarnings("PMD.UseObjectForClearerAPI")
public interface FeesAndPaymentsClient {

    @ApiOperation("Returns Petition Issue Fee")
    @GetMapping(value = "/fees-register/fees/lookup",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    FeeResponse getPetitionIssueFee(
        @RequestParam(FeesAndPayConstants.CHANNEL) final String channel,
        @RequestParam(FeesAndPayConstants.EVENT) final String event,
        @RequestParam(FeesAndPayConstants.JURISDICTION_1) final String jurisdiction1,
        @RequestParam(FeesAndPayConstants.JURISDICTION_2) final String jurisdiction2,
        @RequestParam(FeesAndPayConstants.SERVICE) final String service,
        @RequestParam(FeesAndPayConstants.KEYWORD) final String keyword
    );
}
