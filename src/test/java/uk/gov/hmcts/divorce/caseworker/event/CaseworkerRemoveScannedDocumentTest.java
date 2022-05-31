package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.COVERSHEET;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.OTHER;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveScannedDocument.CASEWORKER_REMOVE_SCANNED_DOCUMENT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerRemoveScannedDocumentTest {

    @InjectMocks
    private CaseworkerRemoveScannedDocument caseworkerRemoveScannedDocument;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveScannedDocument.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REMOVE_SCANNED_DOCUMENT);
    }

    @Test
    void shouldRemoveScannedDocumentFromList() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", FORM);

        final ListValue<ScannedDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", COVERSHEET);

        final ListValue<ScannedDocument> doc3 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005", "application.pdf", OTHER);

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData previousCaseData = caseData();
        previousCaseData.getDocuments().setScannedDocuments(List.of(doc1, doc2, doc3));
        previousCaseDetails.setData(previousCaseData);

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getDocuments().setScannedDocuments(List.of(doc2));
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveScannedDocument.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        final List<ListValue<ScannedDocument>> actualDocuments = response.getData().getDocuments().getScannedDocuments();
        assertThat(actualDocuments.size()).isEqualTo(1);
        assertThat(actualDocuments.get(0).getValue()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldReturnErrorIfDocumentHasBeenAdded() {

        final ListValue<ScannedDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", FORM);

        final ListValue<ScannedDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", COVERSHEET);

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        final CaseData previousCaseData = caseData();
        previousCaseData.getDocuments().setScannedDocuments(List.of(doc1));
        previousCaseDetails.setData(previousCaseData);

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getDocuments().setScannedDocuments(List.of(doc2));
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveScannedDocument.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0))
            .isEqualTo("Scanned documents cannot be added by 'Remove scanned documents'");
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