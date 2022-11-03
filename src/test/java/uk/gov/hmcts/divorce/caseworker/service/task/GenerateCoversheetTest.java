package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;


@ExtendWith(MockitoExtension.class)
public class GenerateCoversheetTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateCoversheet generateCoversheet;

    @Test
    void shouldGenerateCoversheet() {
        setMockClock(clock);

        CaseData caseData = new CaseData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);

        Map<String, Object> templateContent = new HashMap<>();

        generateCoversheet.generateCoversheet(
            caseData,
            TEST_CASE_ID,
            COVERSHEET_APPLICANT2_SOLICITOR,
            templateContent,
            caseData.getApplicant2().getLanguagePreference()
        );

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                COVERSHEET,
                templateContent,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT2_SOLICITOR,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, now(clock))
            );
    }

    @Test
    void shouldGenerateCoversheetWithCustomFileName() {
        setMockClock(clock);

        CaseData caseData = new CaseData();
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);

        Map<String, Object> templateContent = new HashMap<>();

        generateCoversheet.generateCoversheet(
            caseData,
            TEST_CASE_ID,
            COVERSHEET_APPLICANT2_SOLICITOR,
            templateContent,
            caseData.getApplicant2().getLanguagePreference(),
            formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                COVERSHEET,
                templateContent,
                TEST_CASE_ID,
                COVERSHEET_APPLICANT2_SOLICITOR,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, COVERSHEET_DOCUMENT_NAME, now(clock))
            );
    }
}
