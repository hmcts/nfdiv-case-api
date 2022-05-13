package uk.gov.hmcts.divorce.divorcecase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.CHERISHED;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.COVERSHEET;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.OTHER;

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
}