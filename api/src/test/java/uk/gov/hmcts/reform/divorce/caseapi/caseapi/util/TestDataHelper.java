package uk.gov.hmcts.reform.divorce.caseapi.caseapi.util;

import uk.gov.hmcts.reform.divorce.caseapi.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;

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
}
