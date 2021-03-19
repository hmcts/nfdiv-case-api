package uk.gov.hmcts.reform.divorce.caseapi.caseapi.util;

import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.caseapi.model.payments.FeeResponse;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeItem;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeValue;
import uk.gov.hmcts.reform.divorce.ccd.model.OrderSummary;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.ccd.model.FeeValue.getValueInPence;

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
            .feeCode("FEECODE1")
            .amount(10.0)
            .description("Issue Fee")
            .version(1)
            .build();
    }

    public static OrderSummary getDefaultOrderSummary() {
        return OrderSummary
            .builder()
            .fees(singletonList(getDefaultFeeItem()))
            .build();
    }


    public static OrderSummary getOrderSummary(FeeItem feeItem) {
        return OrderSummary
            .builder()
            .fees(singletonList(feeItem))
            .paymentTotal(feeItem.getValue().getFeeAmount())
            .build();
    }


    public static FeeItem getFeeItem(double feeAmount, String feeCode, String description, int version) {
        return FeeItem
            .builder()
            .value(
                FeeValue
                    .builder()
                    .feeAmount(getValueInPence(feeAmount))
                    .feeCode(feeCode)
                    .feeDescription(description)
                    .feeVersion(String.valueOf(version))
                    .build()
            )
            .build();
    }

    public static FeeItem getDefaultFeeItem() {
        return getFeeItem(10.50, "FEECODE1", "Issue Petition Fee", 1);
    }

}
