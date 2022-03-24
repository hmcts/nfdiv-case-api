package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.divorcecase.search.CaseFieldsConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CoversheetApplicant1TemplateContentTest {

    @InjectMocks
    private CoversheetApplicant1TemplateContent coversheetApplicant1TemplateContent;

    @Test
    void shouldReturnCoversheetTemplateContent() {

        final CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(APPLICANT_1_FIRST_NAME);
        caseData.getApplicant1().setLastName(APPLICANT_1_LAST_NAME);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line 1")
                .postCode("postcode")
                .build()
        );
        final Map<String, Object> result = coversheetApplicant1TemplateContent.apply(caseData, TEST_CASE_ID);

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("caseReference", formatId(1616591401473378L));
        expectedEntries.put("applicantFirstName", APPLICANT_1_FIRST_NAME);
        expectedEntries.put("applicantLastName", APPLICANT_1_LAST_NAME);
        expectedEntries.put("applicantAddress", "line 1\npostcode");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
