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
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseDataDocumentServiceTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String GENERAL_ORDER_PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private CaseDataDocumentService caseDataDocumentService;

    @Test
    void shouldAddRenderedDocumentToCaseData() {

        final var documentId = "123456";
        final CaseData caseData = caseData();
        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;
        final User systemUser = mock(User.class);
        final String filename = DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_DRAFT_APPLICATION,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, PDF_FILENAME, DOC_BINARY_URL));

        when(documentIdProvider.documentId()).thenReturn(documentId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            EMAIL,
            templateContentSupplier,
            TEST_CASE_ID,
            DIVORCE_DRAFT_APPLICATION,
            ENGLISH,
            filename);

        final List<ListValue<DivorceDocument>> documentsGenerated = caseData.getDocumentsGenerated();

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

        final Supplier<Map<String, Object>> templateContentSupplier = HashMap::new;
        final User systemUser = mock(User.class);
        final String filename = GENERAL_ORDER_PDF_FILENAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContentSupplier,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_GENERAL_ORDER,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, PDF_FILENAME, DOC_BINARY_URL));

        final Document result = caseDataDocumentService.renderDocument(
            templateContentSupplier,
            TEST_CASE_ID,
            DIVORCE_GENERAL_ORDER,
            ENGLISH,
            filename);

        assertThat(result.getBinaryUrl()).isEqualTo(DOC_BINARY_URL);
        assertThat(result.getUrl()).isEqualTo(DOC_URL);
        assertThat(result.getFilename()).isEqualTo(GENERAL_ORDER_PDF_FILENAME);
    }
}
