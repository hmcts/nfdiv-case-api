package uk.gov.hmcts.divorce.document;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
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
    public void shouldGenerateAndStoreCaseDocument() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));

        Document foDocument = Document.builder()
            .filename("final-order.pdf")
            .build();


        when(templateContent2.getSupportedTemplates()).thenReturn(List.of(FINAL_ORDER_TEMPLATE_ID));
        when(templateContent2.getTemplateContent(any(), anyLong(), any()))
            .thenReturn(SUCCESS_MAP);
        when(caseDataDocumentService.renderDocument(any(), anyLong(), any(), any(), any()))
            .thenReturn(foDocument);

        CaseData data = validApplicant1CaseData();

        documentGenerator.generateAndStoreCaseDocument(
            FINAL_ORDER_GRANTED,
            FINAL_ORDER_TEMPLATE_ID,
            FINAL_ORDER_DOCUMENT_NAME,
            data,
            TEST_CASE_ID
        );

        verify(caseDataDocumentService).renderDocument(
            eq(SUCCESS_MAP),
            eq(TEST_CASE_ID),
            eq(FINAL_ORDER_TEMPLATE_ID),
            eq(LanguagePreference.ENGLISH),
            any()
        );
        verify(caseDataDocumentService).updateCaseData(
            any(),
            eq(FINAL_ORDER_GRANTED),
            eq(foDocument),
            anyLong(),
            any()
        );
    }

    @Test
    void shouldGenerateCertificateOfEntitlementDocument() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData()).id(TEST_CASE_ID).build();

        final CaseData caseData = buildCaseDataWithDocuments();

        caseDetails.setData(caseData);

        when(templateContent2.getSupportedTemplates()).thenReturn(List.of(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID));

        documentGenerator.generateCertificateOfEntitlement(caseDetails);

        assertEquals(CERTIFICATE_OF_ENTITLEMENT, caseData.getConditionalOrder().getCertificateOfEntitlementDocument().getDocumentType());
    }

    private CaseData buildCaseDataWithDocuments() {
        final CaseData caseData = caseData();
        caseData.setDocuments(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                        ListValue.<DivorceDocument>builder()
                                .id("1")
                                .value(DivorceDocument.builder()
                                        .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1)
                                        .build())
                                .build(),
                        ListValue.<DivorceDocument>builder()
                                .id("2")
                                .value(DivorceDocument.builder()
                                        .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2)
                                        .build())
                                .build(),
                        ListValue.<DivorceDocument>builder()
                                .id("3")
                                .value(DivorceDocument.builder()
                                        .documentType(CERTIFICATE_OF_ENTITLEMENT)
                                        .build()).build()
                ))
                .build());
        return caseData;
    }

    private DocumentPackInfo getDocumentPackInfo() {
        return new DocumentPackInfo(
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
