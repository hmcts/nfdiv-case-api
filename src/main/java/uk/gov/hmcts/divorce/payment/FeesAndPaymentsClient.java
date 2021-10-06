package uk.gov.hmcts.divorce.payment;

import io.swagger.annotations.ApiOperation;
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

    @ApiOperation("Returns Application Issue Fee")
    @GetMapping(value = "/fees-register/fees/lookup",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    FeeResponse getApplicationIssueFee(
        @RequestParam(CHANNEL) final String channel,
        @RequestParam(EVENT) final String event,
        @RequestParam(JURISDICTION_1) final String jurisdiction1,
        @RequestParam(JURISDICTION_2) final String jurisdiction2,
        @RequestParam(SERVICE) final String service,
        @RequestParam(KEYWORD) final String keyword
    );
}

// FEE0392 - Bailiff (45)
//    private static final String FINANCIALORDER_FEE = "channel=default&event=miscellaneous&jurisdiction1=family&"
//            + "jurisdiction2=family%20court&keyword=financial-order&service=other";


// FEE0228 - (50)
//    private static final String APPLICATION_NO_NOTICE = "channel=default&event=general%20application&jurisdiction1=family&"
//            + "jurisdiction2=family%20court&keyword=GeneralAppWithoutNotice&service=other";
