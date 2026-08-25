package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;

@ExtendWith(MockitoExtension.class)
class CertificateOfServiceContentTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private CertificateOfServiceContent certificateOfServiceContent;

    @Test
    void shouldReturnTemplateContentForCertificateOfService() {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.setApplicant2(getApplicant2(MALE));

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put(CCD_CASE_REFERENCE, 1616591401473378L);
        expectedEntries.put(PETITIONER_FULL_NAME, TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put(RESPONDENT_FULL_NAME, TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put(DOCUMENTS_ISSUED_ON, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        expectedEntries.put(IS_DIVORCE, "Yes");
        expectedEntries.put(DIVORCE_OR_DISSOLUTION, DIVORCE_APPLICATION);

        Map<String, Object> templateContent = certificateOfServiceContent.apply(caseData, 1616591401473378L);

        assertThat(templateContent).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }
}
