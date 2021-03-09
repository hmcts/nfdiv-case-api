package uk.gov.hmcts.reform.divorce.caseapi.caseapi.util;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.caseapi.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.D8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.D8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.caseapi.TestConstants.TEST_USER_EMAIL;

public class TestDataHelper {
    private TestDataHelper(){

    }

    public static CcdCallbackRequest ccdCallbackRequest() {
        Map<String, Object> caseData = Map.of(
            D8_PETITIONER_FIRST_NAME, TEST_FIRST_NAME,
            D8_PETITIONER_LAST_NAME, TEST_LAST_NAME,
            D8_PETITIONER_EMAIL, TEST_USER_EMAIL,
            DIVORCE_OR_DISSOLUTION, DivorceOrDissolution.DIVORCE
        );

        return CcdCallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(caseData)
                    .build()
            )
            .build();
    }

    public static CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setD8PetitionerFirstName(TEST_FIRST_NAME);
        caseData.setD8PetitionerLastName(TEST_LAST_NAME);
        caseData.setD8PetitionerEmail(TEST_USER_EMAIL);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        return caseData;
    }
}
