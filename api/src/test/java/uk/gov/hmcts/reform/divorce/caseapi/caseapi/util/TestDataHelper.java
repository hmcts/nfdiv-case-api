package uk.gov.hmcts.reform.divorce.caseapi.caseapi.util;

import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.model.payments.FeeResponse;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ccd.sdk.type.Fee.getValueInPence;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.FEE_CODE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.ISSUE_FEE;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;

public class TestDataHelper {

    private TestDataHelper() {

    }

    public static CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setD8PetitionerFirstName(TEST_FIRST_NAME);
        caseData.setD8PetitionerLastName(TEST_LAST_NAME);
        caseData.setD8PetitionerEmail(TEST_USER_EMAIL);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        return caseData;
    }

    public static CcdCallbackRequest callbackRequest() {
        return CcdCallbackRequest
            .builder()
            .caseDetails(
                CaseDetails
                    .builder()
                    .caseData(caseData())
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
}
