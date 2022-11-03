package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.EMAIL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseDataDocumentServiceTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String GENERAL_ORDER_PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String NOP_PDF_FILENAME = "noticeOfProceedings-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";
    private static final String GENERAL_LETTER_PDF = "GeneralLetter-2020-04-20:12:21.pdf";

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
        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = DIVORCE_DRAFT_APPLICATION_DOCUMENT_NAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
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
            templateContent,
            TEST_CASE_ID,
            DIVORCE_DRAFT_APPLICATION,
            ENGLISH,
            filename);

        final List<ListValue<DivorceDocument>> documentsGenerated = caseData.getDocuments().getDocumentsGenerated();

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

        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = GENERAL_ORDER_PDF_FILENAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_GENERAL_ORDER,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, PDF_FILENAME, DOC_BINARY_URL));

        final Document result = caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            DIVORCE_GENERAL_ORDER,
            ENGLISH,
            filename);

        assertThat(result.getBinaryUrl()).isEqualTo(DOC_BINARY_URL);
        assertThat(result.getUrl()).isEqualTo(DOC_URL);
        assertThat(result.getFilename()).isEqualTo(GENERAL_ORDER_PDF_FILENAME);
    }

    @Test
    public void shouldGenerateConfidentialDocumentsWhenDocumentTypeIsNOPAndApplicant1ContactIsPrivate() {
        final var documentId = "123456";
        final CaseData caseData = caseData();
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);

        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                NFD_NOP_A1_SOLE_APP1_CIT_CS,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, NOP_PDF_FILENAME, DOC_BINARY_URL));

        when(documentIdProvider.documentId()).thenReturn(documentId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_1,
            templateContent,
            TEST_CASE_ID,
            NFD_NOP_A1_SOLE_APP1_CIT_CS,
            ENGLISH,
            filename);

        final List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        final List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments
            = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertTrue(isEmpty(nonConfidentialDocuments));
        assertThat(confidentialDocuments).hasSize(1);

        final ListValue<ConfidentialDivorceDocument> documentListValue = confidentialDocuments.get(0);
        final var confidentialDivorceDocument = documentListValue.getValue();

        assertThat(documentListValue.getId()).isEqualTo(documentId);
        assertThat(confidentialDivorceDocument.getConfidentialDocumentsReceived())
            .isEqualTo(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1);
        assertThat(confidentialDivorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                NOP_PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    public void shouldGenerateConfidentialDocumentsWhenDocumentTypeIsNOPAndApplicant2ContactIsPrivate() {
        final var documentId = "123456";
        final CaseData caseData = caseData();
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);

        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                NFD_NOP_A1_SOLE_APP1_CIT_CS,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, NOP_PDF_FILENAME, DOC_BINARY_URL));

        when(documentIdProvider.documentId()).thenReturn(documentId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            NOTICE_OF_PROCEEDINGS_APP_2,
            templateContent,
            TEST_CASE_ID,
            NFD_NOP_A1_SOLE_APP1_CIT_CS,
            ENGLISH,
            filename);

        final List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        final List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments
            = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertTrue(isEmpty(nonConfidentialDocuments));
        assertThat(confidentialDocuments).hasSize(1);

        final ListValue<ConfidentialDivorceDocument> documentListValue = confidentialDocuments.get(0);
        final var confidentialDivorceDocument = documentListValue.getValue();

        assertThat(documentListValue.getId()).isEqualTo(documentId);
        assertThat(confidentialDivorceDocument.getConfidentialDocumentsReceived())
            .isEqualTo(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2);
        assertThat(confidentialDivorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                NOP_PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    public void shouldGenerateConfidentialDocumentsWhenDocumentTypeIsGeneralLetterAndApplicant1ContactIsPrivate() {
        final var documentId = "123456";
        final CaseData caseData = caseData();
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseData.getGeneralLetter().setGeneralLetterParties(GeneralParties.APPLICANT);

        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = GENERAL_LETTER_DOCUMENT_NAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                GENERAL_LETTER_TEMPLATE_ID,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, GENERAL_LETTER_PDF, DOC_BINARY_URL));

        when(documentIdProvider.documentId()).thenReturn(documentId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            GENERAL_LETTER,
            templateContent,
            TEST_CASE_ID,
            GENERAL_LETTER_TEMPLATE_ID,
            ENGLISH,
            filename);

        final List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        final List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments
            = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertTrue(isEmpty(nonConfidentialDocuments));
        assertThat(confidentialDocuments).hasSize(1);

        final ListValue<ConfidentialDivorceDocument> documentListValue = confidentialDocuments.get(0);
        final var confidentialDivorceDocument = documentListValue.getValue();

        assertThat(documentListValue.getId()).isEqualTo(documentId);
        assertThat(confidentialDivorceDocument.getConfidentialDocumentsReceived())
            .isEqualTo(ConfidentialDocumentsReceived.GENERAL_LETTER);
        assertThat(confidentialDivorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                GENERAL_LETTER_PDF,
                DOC_BINARY_URL);
    }

    @Test
    public void shouldGenerateConfidentialDocumentsWhenDocumentTypeIsOther() {
        final var documentId = "123456";
        final CaseData caseData = caseData();
        caseData.getGeneralLetter().setGeneralLetterParties(GeneralParties.OTHER);

        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = "some_filename.pdf";

        MockedStatic<DocumentUtil> classMock = mockStatic(DocumentUtil.class);
        classMock.when(() -> DocumentUtil.isConfidential(caseData, DocumentType.OTHER)).thenReturn(true);
        classMock.when(() -> DocumentUtil.getConfidentialDocumentType(DocumentType.OTHER))
            .thenReturn(ConfidentialDocumentsReceived.OTHER);

        var confidentialDoc = ConfidentialDivorceDocument
            .builder()
            .documentLink(
                Document
                    .builder()
                    .filename("some_pdf.pdf")
                    .url("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003")
                    .binaryUrl("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary")
                    .build()
            )
            .documentFileName(filename)
            .confidentialDocumentsReceived(ConfidentialDocumentsReceived.OTHER)
            .build();

        classMock.when(() -> DocumentUtil.divorceDocumentFrom(any(DocumentInfo.class), any(ConfidentialDocumentsReceived.class)))
            .thenReturn(confidentialDoc);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                "some_template_id",
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, "some_pdf.pdf", DOC_BINARY_URL));

        when(documentIdProvider.documentId()).thenReturn(documentId);

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            DocumentType.OTHER,
            templateContent,
            TEST_CASE_ID,
            "some_template_id",
            ENGLISH,
            filename);

        final List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        final List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments
            = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertTrue(isEmpty(nonConfidentialDocuments));
        assertThat(confidentialDocuments).hasSize(1);

        final ListValue<ConfidentialDivorceDocument> documentListValue = confidentialDocuments.get(0);
        final var confidentialDivorceDocument = documentListValue.getValue();

        assertThat(documentListValue.getId()).isEqualTo(documentId);
        assertThat(confidentialDivorceDocument.getConfidentialDocumentsReceived())
            .isEqualTo(ConfidentialDocumentsReceived.OTHER);
        assertThat(confidentialDivorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                "some_pdf.pdf",
                DOC_BINARY_URL);

        classMock.close();
    }
}
