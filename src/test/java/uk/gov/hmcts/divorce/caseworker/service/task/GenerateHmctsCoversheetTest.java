package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_HMCTS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.HMCTS_COVERSHEET_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.HMCTS_COVERSHEET;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;


@ExtendWith(MockitoExtension.class)
class GenerateHmctsCoversheetTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateHmctsCoversheet generateHmctsCoversheet;

    @Test
    void shouldReturnTrueWhenCoversheetAlreadyGenerated() {
        CaseData caseData = caseData();
        ListValue<DivorceDocument> hmctsCoversheet = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(HMCTS_COVERSHEET)
                .build())
            .build();
        caseData.getDocuments().setDocumentsGenerated(List.of(hmctsCoversheet));

        assertTrue(generateHmctsCoversheet.hasHmctsCoverSheet(caseData));
    }

    @Test
    void shouldReturnFalseWhenCoversheetNotAlreadyGenerated() {
        assertFalse(generateHmctsCoversheet.hasHmctsCoverSheet(caseData()));
    }

    @Test
    void shouldGenerateHmctsCoversheet() {
        setMockClock(clock);

        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH)).thenReturn(getBasicDocmosisTemplateContent(ENGLISH));

        generateHmctsCoversheet.addToDocumentsGenerated(caseDetails);

        Map<String, Object> templateContent = getBasicDocmosisTemplateContent(LanguagePreference.ENGLISH);
        templateContent.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        verify(caseDataDocumentService)
            .renderDocumentAndUpdateCaseData(
                caseData,
                HMCTS_COVERSHEET,
                templateContent,
                TEST_CASE_ID,
                COVERSHEET_HMCTS,
                ENGLISH,
                formatDocumentName(TEST_CASE_ID, HMCTS_COVERSHEET_NAME, now(clock))
            );
    }

    @Test
    void shouldNotGenerateHmctsCoversheetIfItAlreadyExists() {
        CaseData caseData = caseData();
        ListValue<DivorceDocument> hmctsCoversheet = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(HMCTS_COVERSHEET)
                .build())
            .build();
        caseData.getDocuments().setDocumentsGenerated(List.of(hmctsCoversheet));

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        generateHmctsCoversheet.addToDocumentsGenerated(caseDetails);

        verifyNoInteractions(caseDataDocumentService);
    }
}
