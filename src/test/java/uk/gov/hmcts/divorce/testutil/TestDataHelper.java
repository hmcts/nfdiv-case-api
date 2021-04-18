package uk.gov.hmcts.divorce.testutil;

import feign.FeignException;
import feign.Request;
import feign.Response;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.CaseDetails;
import uk.gov.hmcts.divorce.common.model.CcdCallbackRequest;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.payment.model.FeeResponse;

import java.util.Collections;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FEE_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

public class TestDataHelper {

    private TestDataHelper() {

    }

    public static CaseData caseData() {
        return CaseData.builder()
            .petitionerFirstName(TEST_FIRST_NAME)
            .petitionerLastName(TEST_LAST_NAME)
            .petitionerEmail(TEST_USER_EMAIL)
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();
    }

    public static CcdCallbackRequest callbackRequest() {
        return callbackRequest(caseData());
    }

    public static CcdCallbackRequest callbackRequest(final CaseData caseData) {
        return CcdCallbackRequest
            .builder()
            .caseDetails(
                CaseDetails
                    .builder()
                    .caseData(caseData)
                    .caseId(TEST_CASE_ID)
                    .build()
            )
            .build();
    }

    public static CcdCallbackRequest callbackRequest(final CaseData caseData, String eventId) {
        return CcdCallbackRequest
            .builder()
            .eventId(eventId)
            .caseDetails(
                CaseDetails
                    .builder()
                    .caseData(caseData)
                    .caseId(TEST_CASE_ID)
                    .build()
            )
            .build();
    }

    public static FeeResponse getFeeResponse() {
        return FeeResponse
            .builder()
            .feeCode(FEE_CODE)
            .amount(10.0)
            .description(ISSUE_FEE)
            .version(1)
            .build();
    }

    public static OrderSummary getDefaultOrderSummary() {
        return OrderSummary
            .builder()
            .fees(singletonList(getDefaultFeeItem()))
            .build();
    }

    public static ListValue<Fee> getFeeItem(double feeAmount, String feeCode, String description, int version) {
        return ListValue
            .<Fee>builder()
            .value(
                Fee
                    .builder()
                    .amount(getValueInPence(feeAmount))
                    .code(feeCode)
                    .description(description)
                    .version(String.valueOf(version))
                    .build()
            )
            .build();
    }

    public static ListValue<Fee> getDefaultFeeItem() {
        return getFeeItem(10.50, FEE_CODE, "Issue Petition Fee", 1);
    }

    public static FeignException feignException(int status, String reason) {
        byte[] emptyBody = {};
        Request request = Request.create(GET, EMPTY, Map.of(), emptyBody, UTF_8, null);

        return FeignException.errorStatus(
            "idamRequestFailed",
            Response.builder()
                .request(request)
                .status(status)
                .headers(Collections.emptyMap())
                .reason(reason)
                .build()
        );
    }
}
