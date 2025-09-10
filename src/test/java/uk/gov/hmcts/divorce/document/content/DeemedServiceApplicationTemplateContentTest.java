package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.InterimApplicationOptions;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class DeemedServiceApplicationTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private DeemedServiceApplicationTemplateContent templateContent;

    private static final String DEEMED_EVIDENCE = "Test evidence";
    private static final String DEEMED_NO_EVIDENCE_STATEMENT = "Statement";

    @Test
    void shouldReturnTemplateContentForEnglish() {
        final CaseData caseData = buildTestData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", TEST_FIRST_NAME);
        expectedEntries.put("deemedEvidenceUploaded", true);
        expectedEntries.put("divorceOrDissolution", "divorce application");
        expectedEntries.put("deemedEvidenceDetails", DEEMED_EVIDENCE);
        expectedEntries.put("deemedNoEvidenceStatement", DEEMED_NO_EVIDENCE_STATEMENT);
        expectedEntries.put("statementOfTruth", "Yes");
        expectedEntries.put("serviceApplicationReceivedDate", "1 January 2023");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnTemplateContentForWelsh() {
        final CaseData caseData = buildTestData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", TEST_FIRST_NAME);
        expectedEntries.put("deemedEvidenceUploaded", true);
        expectedEntries.put("divorceOrDissolution", "cais am ysgariad");
        expectedEntries.put("deemedEvidenceDetails", DEEMED_EVIDENCE);
        expectedEntries.put("deemedNoEvidenceStatement", DEEMED_NO_EVIDENCE_STATEMENT);
        expectedEntries.put("statementOfTruth", "Ydw");
        expectedEntries.put("serviceApplicationReceivedDate", "1 Ionawr 2023");

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    private CaseData buildTestData() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        caseData.getApplicant1().setInterimApplicationOptions(
            InterimApplicationOptions.builder()
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .deemedServiceJourneyOptions(
                    DeemedServiceJourneyOptions.builder()
                        .deemedEvidenceDetails(DEEMED_EVIDENCE)
                        .deemedNoEvidenceStatement(DEEMED_NO_EVIDENCE_STATEMENT)
                        .build()
                ).build()
        );
        caseData.setAlternativeService(
            AlternativeService.builder()
                .receivedServiceApplicationDate(LocalDate.of(2023, 1, 1))
                .build()
        );

        return caseData;
    }
}
