package uk.gov.hmcts.reform.divorce.caseapi.clients;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.caseapi.model.payments.FeeResponse;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "fees-and-payments-client", url = "${fee.api.baseurl}")
public interface FeesAndPaymentsClient {

    @ApiOperation("Returns Petition Issue Fee")
    @GetMapping(value = "/fees-register/fees/lookup",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    FeeResponse getPetitionIssueFee(
        @RequestParam("channel") final String channel,
        @RequestParam("event") final String event,
        @RequestParam("jurisdiction1") final String jurisdiction1,
        @RequestParam("jurisdiction2") final String jurisdiction2,
        @RequestParam("service") final String service,
        @RequestParam("keyword") final String keyword
    );
}
