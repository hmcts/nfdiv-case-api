package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderPronouncedTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class GenerateConditionalOrderPronouncedDocumentTest {

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderPronouncedTemplateContent conditionalOrderPronouncedTemplateContent;

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Test
    void shouldGenerateConditionalOrderGrantedDocAndUpdateCaseData() {

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        generateConditionalOrderPronouncedDocument.apply(caseDetails);

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED,
            templateContent,
            TEST_CASE_ID,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
            ENGLISH,
            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME);
    }

    @Test
    public void shouldReturnConditionalOrderDocWhenExists() {

        ListValue<DivorceDocument> divorceDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        ListValue<DivorceDocument> coDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(CONDITIONAL_ORDER_GRANTED.getLabel())
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsGenerated(List.of(divorceDocumentListValue, coDocumentListValue))
                .build())
            .build();

        Optional<ListValue<DivorceDocument>> conditionalOrderGrantedDoc =
            generateConditionalOrderPronouncedDocument.getConditionalOrderGrantedDoc(caseData);

        assertTrue(conditionalOrderGrantedDoc.isPresent());
    }

    @Test
    public void shouldNotReturnConditionalOrderDocWhenDoesNotExists() {
        ListValue<DivorceDocument> divorceDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsGenerated(singletonList(divorceDocumentListValue))
                .build())
            .build();

        Optional<ListValue<DivorceDocument>> conditionalOrderGrantedDoc =
            generateConditionalOrderPronouncedDocument.getConditionalOrderGrantedDoc(caseData);

        assertTrue(conditionalOrderGrantedDoc.isEmpty());
    }

    @Test
    public void shouldRemoveConditionalOrderGrantedDoc() {
        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .languagePreferenceWelsh(NO)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderPronouncedTemplateContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        generateConditionalOrderPronouncedDocument
            .removeExistingAndGenerateNewConditionalOrderGrantedDoc(caseDetails);

        verify(documentRemovalService).deleteDocumentFromDocumentStore(
            caseData.getDocuments().getDocumentsGenerated(),
            CONDITIONAL_ORDER_GRANTED,
            TEST_CASE_ID
        );

        verify(caseDataDocumentService).renderDocumentAndUpdateCaseData(
            caseData,
            CONDITIONAL_ORDER_GRANTED,
            templateContent,
            TEST_CASE_ID,
            CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID,
            ENGLISH,
            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME);
    }

}
