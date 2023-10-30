package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratorTest {

    private static final Map<String, Object> SUCCESS_MAP = Map.of("Success", true);

    @Mock
    private TemplateContent templateContent1;

    @Mock
    private TemplateContent templateContent2;

    @Mock
    private TemplateContent templateContent3;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private Clock clock;

    private DocumentGenerator documentGenerator;

    @BeforeEach
    public void setUp() {
        documentGenerator = new DocumentGenerator(
            Arrays.asList(templateContent1, templateContent2, templateContent3),
            caseDataDocumentService,
            clock
        );
    }

    @Test
    void shouldReturnListOfLettersWhenGenerateDocumentsCalledWithCorrectParams() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
        CaseData data = validApplicant1CaseData();

        data.getDocuments().setDocumentsGenerated(List.of(
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(DocumentType.APPLICATION)
                    .documentLink(Document.builder()
                        .filename("application.pdf")
                        .build())
                    .build())
            .build()));
        long caseId = TEST_CASE_ID;
        Applicant applicant = data.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        when(caseDataDocumentService.renderDocument(eq(SUCCESS_MAP), any(), eq(COVERSHEET_APPLICANT), any(), any()))
            .thenReturn(Document.builder().filename(COVERSHEET_DOCUMENT_NAME).build());

        when(templateContent2.getSupportedTemplates()).thenReturn(List.of(COVERSHEET_APPLICANT));
        when(templateContent2.getTemplateContent(any(), any(), any())).thenReturn(SUCCESS_MAP);

        List<Letter> letters = documentGenerator.generateDocuments(data, caseId, applicant, documentPackInfo);

        assertThat(letters.stream().map(this::extractFilenameFromLetter))
            .containsExactly(COVERSHEET_DOCUMENT_NAME, "application.pdf");

        verify(templateContent2, times(1)).getTemplateContent(any(), any(), any());
        verify(caseDataDocumentService, times(1))
            .renderDocument(eq(SUCCESS_MAP), any(), eq(COVERSHEET_APPLICANT), any(), any());
        verify(caseDataDocumentService, times(1)).updateCaseData(any(), any(), any(Document.class), any(), any());
        verifyNoMoreInteractions(caseDataDocumentService);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenLocatingCorrectTemplateContentBeanFailsToYieldOneResult() {
        CaseData data = validApplicant1CaseData();

        data.getDocuments().setDocumentsGenerated(List.of(
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(DocumentType.APPLICATION)
                    .documentLink(Document.builder()
                        .filename("application.pdf")
                        .build())
                    .build())
                .build()));
        long caseId = TEST_CASE_ID;
        Applicant applicant = data.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        when(templateContent2.getSupportedTemplates()).thenReturn(List.of(COVERSHEET_APPLICANT));
        when(templateContent3.getSupportedTemplates()).thenReturn(List.of(COVERSHEET_APPLICANT));

        assertThrows(IllegalStateException.class, () -> documentGenerator.generateDocuments(data, caseId, applicant, documentPackInfo));
    }

    @Test
    public void shouldReturnListWithNullsRemovedIfDocTypeMissingFromCaseData() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
        CaseData data = validApplicant1CaseData();

        data.getDocuments().setDocumentsGenerated(Collections.emptyList());
        long caseId = TEST_CASE_ID;
        Applicant applicant = data.getApplicant1();
        DocumentPackInfo documentPackInfo = getDocumentPackInfo();

        when(caseDataDocumentService.renderDocument(eq(SUCCESS_MAP), any(), eq(COVERSHEET_APPLICANT), any(), any()))
            .thenReturn(Document.builder().filename(COVERSHEET_DOCUMENT_NAME).build());

        when(templateContent2.getSupportedTemplates()).thenReturn(List.of(COVERSHEET_APPLICANT));
        when(templateContent2.getTemplateContent(any(), any(), any())).thenReturn(SUCCESS_MAP);

        List<Letter> letters = documentGenerator.generateDocuments(data, caseId, applicant, documentPackInfo);

        assertThat(letters.stream().map(this::extractFilenameFromLetter))
            .containsExactly(COVERSHEET_DOCUMENT_NAME);
    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationPack() {


    }

    @Test
    void shouldGenerateAwaitingClarificationPack() {

    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingClarificationApplicationPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationSolicitorPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingClarificationApplicationSolicitorPackForJudicialSeparation() {

    }

    @Test
    void shouldGenerateAwaitingAmendedApplicationPackForSeparationOrder() {

    }

    @Test
    void shouldGenerateAwaitingClarificationPackForSeparationOrder() {

    }

    @Test
    void shouldFetchRefusalLetterWhenContactDetailsArePrivate() {

    }

    private DocumentPackInfo getDocumentPackInfo() {
        return DocumentPackInfo.of(
            ImmutableMap.of(
                DocumentType.COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
                DocumentType.APPLICATION, Optional.empty()
            ),
            ImmutableMap.of(
                COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME
            )
        );
    }

    private String extractFilenameFromLetter(Letter letter) {
        if (letter.getDocument() != null) {
            return letter.getDocument().getFilename();
        } else if (letter.getDivorceDocument() != null) {
            return letter.getDivorceDocument().getDocumentLink().getFilename();
        } else {
            return letter.getConfidentialDivorceDocument().getDocumentLink().getFilename();
        }
    }
}
