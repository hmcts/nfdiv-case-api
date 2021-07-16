package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseDataDocumentServiceTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-mini-application-1616591401473378.pdf";
    private static final String GENERAL_ORDER_PDF_FILENAME = "draft-mini-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @InjectMocks
    private CaseDataDocumentService caseDataDocumentService;

    @Test
    void shouldAddRenderedDocumentToCaseData() {

        final var documentId = "123456";
        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        when(docAssemblyService
            .renderDocument(
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_MINI_DRAFT_APPLICATION,
                DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME,
                ENGLISH))
            .thenReturn(new DocumentInfo(DOC_URL, PDF_FILENAME, DOC_BINARY_URL));

        when(documentIdProvider.documentId()).thenReturn(documentId);

        final CaseData result = caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData(),
            EMAIL,
            templateContentSupplier,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            DIVORCE_MINI_DRAFT_APPLICATION,
            DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME,
            ENGLISH);

        final List<ListValue<DivorceDocument>> documentsGenerated = result.getDocumentsGenerated();

        assertThat(documentsGenerated).hasSize(1);

        final ListValue<DivorceDocument> documentListValue = documentsGenerated.get(0);
        final var divorceDocument = documentListValue.getValue();

        assertThat(documentListValue.getId()).isEqualTo(documentId);
        assertThat(divorceDocument.getDocumentType()).isEqualTo(EMAIL);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldGenerateAndReturnGeneralOrderDocument() {

        final var documentId = "123456";
        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;

        when(docAssemblyService
            .renderDocument(
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_GENERAL_ORDER,
                GENERAL_ORDER_PDF_FILENAME,
                ENGLISH))
            .thenReturn(new DocumentInfo(DOC_URL, PDF_FILENAME, DOC_BINARY_URL));

        final Document result = caseDataDocumentService.renderGeneralOrderDocument(
            templateContentSupplier,
            TEST_CASE_ID,
            TEST_AUTHORIZATION_TOKEN,
            DIVORCE_GENERAL_ORDER,
            GENERAL_ORDER_PDF_FILENAME,
            ENGLISH);

        assertThat(result.getBinaryUrl()).isEqualTo(DOC_BINARY_URL);
        assertThat(result.getUrl()).isEqualTo(DOC_URL);
        assertThat(result.getFilename()).isEqualTo(GENERAL_ORDER_PDF_FILENAME);
    }
}
