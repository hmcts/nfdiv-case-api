package uk.gov.hmcts.divorce.divorcecase.model;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.CHERISHED;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.COVERSHEET;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.OTHER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AMENDED_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;

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

    @Test
    public void shouldRemoveGivenDocumentType() {
        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(
                ListValue.<DivorceDocument>builder()
                    .id("1")
                    .value(DivorceDocument.builder()
                        .documentType(APPLICATION)
                        .build())
                    .build(),
                ListValue.<DivorceDocument>builder()
                    .id("2")
                    .value(DivorceDocument.builder()
                        .documentType(AMENDED_APPLICATION)
                        .build()).build()
            ))
            .build();

        caseDocuments.removeDocumentGeneratedWithType(APPLICATION);

        assertEquals(1, caseDocuments.getDocumentsGenerated().size());
        assertEquals(AMENDED_APPLICATION, caseDocuments.getDocumentsGenerated().get(0).getValue().getDocumentType());
    }

    @Test
    void shouldReturnTrueWhenDocRemoved() {
        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(
                ListValue.<DivorceDocument>builder()
                    .id("1")
                    .value(DivorceDocument.builder()
                        .documentType(APPLICATION)
                        .build())
                    .build(),
                ListValue.<DivorceDocument>builder()
                    .id("2")
                    .value(DivorceDocument.builder()
                        .documentType(AMENDED_APPLICATION)
                        .build()).build()
            ))
            .build();

        assertTrue(caseDocuments.removeDocumentGeneratedWithType(APPLICATION));
    }

    @Test
    void shouldReturnFirstUploadedDocumentOfGivenType() {

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
            .documentsUploaded(List.of(documentListValue4, documentListValue2, documentListValue3, documentListValue1))
            .build();

        assertThat(caseDocuments.getFirstUploadedDocumentLinkWith(DISPENSE_WITH_SERVICE_GRANTED)).isEqualTo(Optional.of(documentLink3));
        assertThat(caseDocuments.getFirstUploadedDocumentLinkWith(DEEMED_AS_SERVICE_GRANTED)).isEqualTo(Optional.of(documentLink4));
    }

    @Test
    void shouldReturnFalseWhenRequiredDocumentsAreNotUnderConfidentialList() {
        final Document documentLink1 = Document.builder()
            .filename("coGrantedCoversheet.pdf")
            .build();
        final Document documentLink2 = Document.builder()
            .filename("coGrantedCoversheet2.pdf")
            .build();
        final Document documentLink3 = Document.builder()
            .filename("coGranted.pdf")
            .build();

        final ListValue<DivorceDocument> documentListValue1 = documentWithType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, documentLink1);
        final ListValue<DivorceDocument> documentListValue2 = documentWithType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, documentLink2);
        final ListValue<DivorceDocument> documentListValue3 = documentWithType(CONDITIONAL_ORDER_GRANTED, documentLink3);

        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(List.of(documentListValue3, documentListValue1, documentListValue2))
            .build();

        assertThat(caseDocuments.isGivenDocumentUnderConfidentialList(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)).isFalse();
        assertThat(caseDocuments.isGivenDocumentUnderConfidentialList(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenRequiredDocumentsAreUnderConfidentialList() {
        final Document documentLink1 = Document.builder()
            .filename("coGrantedCoversheet.pdf")
            .build();
        final Document documentLink2 = Document.builder()
            .filename("coGrantedCoversheet2.pdf")
            .build();
        final Document documentLink3 = Document.builder()
            .filename("coGranted.pdf")
            .build();

        final ListValue<ConfidentialDivorceDocument> documentListValue1
            = documentWithType(ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1, documentLink1);
        final ListValue<ConfidentialDivorceDocument> documentListValue2
            = documentWithType(ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2, documentLink2);
        final ListValue<DivorceDocument> documentListValue3 = documentWithType(CONDITIONAL_ORDER_GRANTED, documentLink3);

        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(List.of(documentListValue3))
            .confidentialDocumentsGenerated(List.of(documentListValue1, documentListValue2))
            .build();

        assertThat(caseDocuments.isGivenDocumentUnderConfidentialList(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)).isTrue();
        assertThat(caseDocuments.isGivenDocumentUnderConfidentialList(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)).isTrue();
    }

    @Test
    void shouldMapScannedDocumentToDivorceDocument() {

        final CaseDocuments caseDocuments = CaseDocuments.builder().build();
        final Clock clock = mock(Clock.class);
        setMockClock(clock);

        final ScannedDocument scannedDocument = ScannedDocument.builder()
            .url(Document.builder().build())
            .fileName("D36.pdf")
            .build();

        final DivorceDocument expectedResponse = DivorceDocument.builder()
            .documentLink(Document.builder().build())
            .documentFileName("D36.pdf")
            .documentDateAdded(LocalDate.now(clock))
            .documentType(FINAL_ORDER_APPLICATION)
            .documentComment("Reclassified scanned document")
            .build();

        assertThat(caseDocuments.mapScannedDocumentToDivorceDocument(scannedDocument, FINAL_ORDER_APPLICATION, clock))
            .isEqualTo(expectedResponse);
    }

    @Test
    public void shouldRemoveGivenConfidentialDocumentType() {
        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .confidentialDocumentsGenerated(Lists.newArrayList(
                ListValue.<ConfidentialDivorceDocument>builder()
                    .id("1")
                    .value(ConfidentialDivorceDocument.builder()
                        .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1)
                        .build())
                    .build(),
                ListValue.<ConfidentialDivorceDocument>builder()
                    .id("2")
                    .value(ConfidentialDivorceDocument.builder()
                        .confidentialDocumentsReceived(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
                        .build()).build()
            ))
            .build();

        caseDocuments.removeConfidentialDocumentGeneratedWithType(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1);

        assertEquals(1, caseDocuments.getConfidentialDocumentsGenerated().size());
        assertEquals(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2,
            caseDocuments.getConfidentialDocumentsGenerated().get(0).getValue().getConfidentialDocumentsReceived());
    }

    @Test
    public void shouldReturnConditionalOrderDocumentWhenExists() {

        final ListValue<DivorceDocument> divorceDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> coDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(CONDITIONAL_ORDER_GRANTED.getLabel())
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(List.of(divorceDocumentListValue, coDocumentListValue))
            .build();

        Optional<ListValue<DivorceDocument>> conditionalOrderGrantedDoc =
            caseDocuments.getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED);

        assertTrue(conditionalOrderGrantedDoc.isPresent());
    }

    @Test
    public void shouldNotReturnConditionalOrderDocumentWhenDoesNotExists() {
        final ListValue<DivorceDocument> divorceDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsGenerated(singletonList(divorceDocumentListValue))
            .build();

        Optional<ListValue<DivorceDocument>> conditionalOrderGrantedDoc =
            caseDocuments.getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED);

        assertTrue(conditionalOrderGrantedDoc.isEmpty());
    }

    @Test
    public void shouldNotReturnConditionalOrderDocumentWhenNoCaseDocuments() {

        final CaseDocuments caseDocuments = CaseDocuments.builder().build();

        Optional<ListValue<DivorceDocument>> conditionalOrderGrantedDoc =
            caseDocuments.getDocumentGeneratedWithType(CONDITIONAL_ORDER_GRANTED);

        assertTrue(conditionalOrderGrantedDoc.isEmpty());
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

    private ListValue<ConfidentialDivorceDocument> documentWithType(
        final ConfidentialDocumentsReceived documentType, final Document document) {

        return ListValue.<ConfidentialDivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(ConfidentialDivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName("test-draft-divorce-application-12345.pdf")
                .confidentialDocumentsReceived(documentType)
                .build())
            .build();
    }
}
