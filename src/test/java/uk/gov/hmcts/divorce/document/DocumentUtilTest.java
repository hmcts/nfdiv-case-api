package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.print.model.Letter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentUtil.convertToLetters;
import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.findDocumentsOfType;
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
    void shouldFindDocumentOfGivenDocumentTypeIfPresent() {

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

        final CaseData caseData = CaseData.builder()
            .documentsGenerated(asList(doc1, doc2))
            .build();

        final List<DivorceDocument> documentsOfType = findDocumentsOfType(caseData, singletonList(MARRIAGE_CERTIFICATE));

        assertThat(documentsOfType.size()).isEqualTo(1);
        assertThat(documentsOfType.get(0).getDocumentType()).isEqualTo(MARRIAGE_CERTIFICATE);
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

        final CaseData caseData = CaseData.builder()
            .documentsGenerated(asList(doc1, doc2))
            .build();

        final List<DivorceDocument> documentsOfType = findDocumentsOfType(caseData, singletonList(NAME_CHANGE_EVIDENCE));

        assertThat(documentsOfType.size()).isZero();
    }

    @Test
    void shouldNotFindDocumentOfGivenDocumentTypeIfNoDocumentsPresent() {

        final CaseData caseData = CaseData.builder().build();

        final List<DivorceDocument> documentsOfType = findDocumentsOfType(caseData, singletonList(NAME_CHANGE_EVIDENCE));

        assertThat(documentsOfType.size()).isZero();
    }

    @Test
    void shouldFindDocumentsOfGivenDocumentTypesIfPresent() {

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

        final CaseData caseData = CaseData.builder()
            .documentsGenerated(asList(doc1, doc2, doc3))
            .build();

        final List<DivorceDocument> documentsOfType = findDocumentsOfType(
            caseData,
            asList(NAME_CHANGE_EVIDENCE, MARRIAGE_CERTIFICATE));

        assertThat(documentsOfType.size()).isEqualTo(2);
        assertThat(documentsOfType.get(0).getDocumentType()).isSameAs(MARRIAGE_CERTIFICATE);
        assertThat(documentsOfType.get(1).getDocumentType()).isSameAs(NAME_CHANGE_EVIDENCE);
    }

    @Test
    void shouldConvertDivorceDocumentsToLetters() {

        final DivorceDocument doc1 = DivorceDocument.builder()
            .documentType(DIVORCE_APPLICATION)
            .build();

        final DivorceDocument doc2 = DivorceDocument.builder()
            .documentType(MARRIAGE_CERTIFICATE)
            .build();

        final DivorceDocument doc3 = DivorceDocument.builder()
            .documentType(NAME_CHANGE_EVIDENCE)
            .build();

        final List<DivorceDocument> divorceDocuments = asList(doc1, doc2, doc3);

        final List<Letter> letters = convertToLetters(divorceDocuments);

        assertThat(letters.size()).isEqualTo(3);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc1);
        assertThat(letters.get(1).getDivorceDocument()).isSameAs(doc2);
        assertThat(letters.get(2).getDivorceDocument()).isSameAs(doc3);
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}
