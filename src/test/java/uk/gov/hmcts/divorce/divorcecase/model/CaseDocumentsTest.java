package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.CHERISHED;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.COVERSHEET;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.OTHER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;

class CaseDocumentsTest {

    @Test
    void shouldReturnTrueIfAfterHasAddedDocument() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final ListValue<ScannedDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", COVERSHEET);

        final ListValue<ScannedDocument> doc3 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005", "application.pdf", OTHER);

        final List<ListValue<ScannedDocument>> before = List.of(doc1, doc2);
        final List<ListValue<ScannedDocument>> after = List.of(doc1, doc2, doc3);

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isTrue();
    }

    @Test
    void shouldReturnTrueIfBeforeIsEmptyAndAfterHasAddedDocument() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final List<ListValue<ScannedDocument>> before = emptyList();
        final List<ListValue<ScannedDocument>> after = List.of(doc1);

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isTrue();
    }

    @Test
    void shouldReturnTrueIfBeforeIsNullAndAfterHasDocument() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final List<ListValue<ScannedDocument>> before = null;
        final List<ListValue<ScannedDocument>> after = List.of(doc1);

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isTrue();
    }

    @Test
    void shouldReturnFalseIfBeforeAndAfterAreTheSame() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final ListValue<ScannedDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", COVERSHEET);

        final ListValue<ScannedDocument> doc3 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005", "application.pdf", OTHER);

        final List<ListValue<ScannedDocument>> before = List.of(doc1, doc2, doc3);
        final List<ListValue<ScannedDocument>> after = List.of(doc1, doc2, doc3);

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFalseIfBeforeAndAfterAreEmpty() {

        final List<ListValue<ScannedDocument>> before = emptyList();
        final List<ListValue<ScannedDocument>> after = emptyList();

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFalseIfBeforeHasDocumentsAndAfterHasRemovedDocument() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final ListValue<ScannedDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", COVERSHEET);

        final ListValue<ScannedDocument> doc3 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005", "application.pdf", OTHER);

        final List<ListValue<ScannedDocument>> before = List.of(doc1, doc2, doc3);
        final List<ListValue<ScannedDocument>> after = List.of(doc1, doc3);

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFalseIfBeforeHasDocumentsAndAfterIsEmpty() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final ListValue<ScannedDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", COVERSHEET);

        final ListValue<ScannedDocument> doc3 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005", "application.pdf", OTHER);

        final List<ListValue<ScannedDocument>> before = List.of(doc1, doc2, doc3);
        final List<ListValue<ScannedDocument>> after = emptyList();

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFalseIfBeforeHasDocumentAndAfterIsNull() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", CHERISHED);

        final List<ListValue<ScannedDocument>> before = List.of(doc1);
        final List<ListValue<ScannedDocument>> after = null;

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFalseIfBeforeIsEmptyAndAfterIsNull() {

        final List<ListValue<ScannedDocument>> before = emptyList();
        final List<ListValue<ScannedDocument>> after = null;

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFalseIfBeforeIsNullAndAfterIsEmpty() {

        final List<ListValue<ScannedDocument>> before = null;
        final List<ListValue<ScannedDocument>> after = emptyList();

        assertThat(CaseDocuments.hasAddedDocuments(after, before)).isFalse();
    }

    @Test
    void shouldReturnFirstGeneratedDocumentOfGivenType() {

        final Document documentLink1 = Document.builder()
            .filename("dispensedDocument1.pdf")
            .build();
        final Document documentLink2 = Document.builder()
            .filename("deemedDocument1.pdf")
            .build();
        final Document documentLink3 = Document.builder()
            .filename("dispensedDocument2.pdf")
            .build();
        final Document documentLink4 = Document.builder()
            .filename("deemedDocument2.pdf")
            .build();

        final ListValue<DivorceDocument> documentListValue1 = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink1);
        final ListValue<DivorceDocument> documentListValue2 = documentWithType(DEEMED_AS_SERVICE_GRANTED, documentLink2);
        final ListValue<DivorceDocument> documentListValue3 = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink3);
        final ListValue<DivorceDocument> documentListValue4 = documentWithType(DEEMED_AS_SERVICE_GRANTED, documentLink4);

        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(List.of(documentListValue4, documentListValue2, documentListValue3, documentListValue1))
            .build();

        assertThat(caseDocuments.getFirstGeneratedDocumentLinkWith(DISPENSE_WITH_SERVICE_GRANTED)).isEqualTo(Optional.of(documentLink3));
        assertThat(caseDocuments.getFirstGeneratedDocumentLinkWith(DEEMED_AS_SERVICE_GRANTED)).isEqualTo(Optional.of(documentLink4));
    }

    private ListValue<ScannedDocument> getDocumentListValue(final String url,
                                                            final String filename,
                                                            final ScannedDocumentType scannedDocumentType) {
        return ListValue.<ScannedDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(ScannedDocument.builder()
                .type(scannedDocumentType)
                .url(Document.builder()
                    .url(url)
                    .filename(filename)
                    .build())
                .build())
            .build();
    }

    private ListValue<DivorceDocument> documentWithType(final DocumentType documentType, final Document document) {

        return ListValue.<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(DivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName("test-draft-divorce-application-12345.pdf")
                .documentType(documentType)
                .build())
            .build();
    }
}