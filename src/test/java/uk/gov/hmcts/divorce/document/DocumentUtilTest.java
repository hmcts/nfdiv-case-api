package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.CaseworkerUploadedDocument;
import uk.gov.hmcts.divorce.document.model.CaseworkerUploadedDocumentType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersOfDocumentTypes;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;

@ExtendWith(MockitoExtension.class)
class DocumentUtilTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-mini-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Test
    void shouldConvertFromDocumentInfoToDocument() {

        final Document document = documentFrom(documentInfo());

        assertThat(document)
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldCreateDivorceDocumentFromDocumentInfoAndDocumentType() {

        final DivorceDocument divorceDocument = divorceDocumentFrom(documentInfo(), OTHER);

        assertThat(divorceDocument.getDocumentType()).isEqualTo(OTHER);
        assertThat(divorceDocument.getDocumentFileName()).isEqualTo(PDF_FILENAME);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldReturnListOfLetterOfGivenDocumentTypeIfPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(DIVORCE_APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final List<Letter> letters = lettersOfDocumentTypes(
            asList(doc1, doc2),
            singletonList(MARRIAGE_CERTIFICATE));

        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldNotFindDocumentOfGivenDocumentTypeIfNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(DIVORCE_APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final List<Letter> letters = lettersOfDocumentTypes(
            asList(doc1, doc2),
            singletonList(NAME_CHANGE_EVIDENCE));

        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfNullDocumentList() {
        final List<Letter> letters = lettersOfDocumentTypes(null, singletonList(NAME_CHANGE_EVIDENCE));
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfEmptyDocumentList() {
        final List<Letter> letters = lettersOfDocumentTypes(emptyList(), singletonList(NAME_CHANGE_EVIDENCE));
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnListOfLettersOfGivenDocumentTypesIfPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(DIVORCE_APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final List<Letter> letters = lettersOfDocumentTypes(
            asList(doc1, doc2, doc3),
            asList(NAME_CHANGE_EVIDENCE, MARRIAGE_CERTIFICATE));

        assertThat(letters.size()).isEqualTo(2);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc2.getValue());
        assertThat(letters.get(1).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldSortDocumentsInDescendingOrderWhenThereAreExistingDocuments() {
        final ListValue<CaseworkerUploadedDocument> existingDocument = ListValue.<CaseworkerUploadedDocument>builder()
            .id("1")
            .value(CaseworkerUploadedDocument.builder()
                .documentType(CaseworkerUploadedDocumentType.D9D)
                .documentDateAdded(LocalDate.of(2021, 1, 1))
                .documentLink(Document
                    .builder()
                    .url(DOC_URL)
                    .filename("d9d.pdf")
                    .binaryUrl(DOC_BINARY_URL)
                    .build()
                )
                .build())
            .build();

        final ListValue<CaseworkerUploadedDocument> newDocument = ListValue.<CaseworkerUploadedDocument>builder()
            .value(CaseworkerUploadedDocument.builder()
                .documentType(CaseworkerUploadedDocumentType.ACKNOWLEDGEMENT_OF_SERVICE)
                .documentDateAdded(LocalDate.of(2021, 2, 2))
                .documentLink(Document
                    .builder()
                    .url(DOC_URL)
                    .filename("aos.pdf")
                    .binaryUrl(DOC_BINARY_URL)
                    .build()
                )
                .build())
            .build();

        List<ListValue<CaseworkerUploadedDocument>> actualDocuments = DocumentUtil.sortDocumentsInDescendingOrder(
            List.of(existingDocument),
            List.of(newDocument, existingDocument)
        );

        assertThat(actualDocuments.size()).isEqualTo(2);
        assertThat(actualDocuments.get(0).getValue()).isSameAs(newDocument.getValue());
        assertThat(actualDocuments.get(1).getValue()).isSameAs(existingDocument.getValue());
    }

    @Test
    void shouldReturnDocumentsInOrderWhenThereAreNoExistingDocuments() {
        final ListValue<CaseworkerUploadedDocument> doc1 = ListValue.<CaseworkerUploadedDocument>builder()
            .id("1")
            .value(CaseworkerUploadedDocument.builder()
                .documentType(CaseworkerUploadedDocumentType.D9D)
                .documentDateAdded(LocalDate.of(2021, 1, 1))
                .documentLink(Document
                    .builder()
                    .url(DOC_URL)
                    .filename("d9d.pdf")
                    .binaryUrl(DOC_BINARY_URL)
                    .build()
                )
                .build())
            .build();

        final ListValue<CaseworkerUploadedDocument> doc2 = ListValue.<CaseworkerUploadedDocument>builder()
            .value(CaseworkerUploadedDocument.builder()
                .documentType(CaseworkerUploadedDocumentType.ACKNOWLEDGEMENT_OF_SERVICE)
                .documentDateAdded(LocalDate.of(2021, 2, 2))
                .documentLink(Document
                    .builder()
                    .url(DOC_URL)
                    .filename("aos.pdf")
                    .binaryUrl(DOC_BINARY_URL)
                    .build()
                )
                .build())
            .build();

        List<ListValue<CaseworkerUploadedDocument>> actualDocuments = DocumentUtil.sortDocumentsInDescendingOrder(
            emptyList(),
            List.of(doc1, doc2)
        );

        assertThat(actualDocuments.size()).isEqualTo(2);
        assertThat(actualDocuments.get(0).getValue()).isSameAs(doc1.getValue());
        assertThat(actualDocuments.get(1).getValue()).isSameAs(doc2.getValue());
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}
