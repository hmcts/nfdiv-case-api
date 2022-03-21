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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUploadDocument.CASEWORKER_UPLOAD_DOCUMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.ACKNOWLEDGEMENT_OF_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D9D;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerUploadDocumentTest {

    @InjectMocks
    private CaseworkerUploadDocument caseworkerUploadDocument;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUploadDocument.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPLOAD_DOCUMENT);
    }

    @Test
    void shouldRetainDocumentsOrderWhenThereAreNoExistingDocuments() {
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(caseData());

        final ListValue<DivorceDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", D9D);

        final ListValue<DivorceDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "aos.pdf", ACKNOWLEDGEMENT_OF_SERVICE);

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getDocuments().setDocumentsUploaded(List.of(doc1, doc2));

        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadDocument.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        List<ListValue<DivorceDocument>> actualDocuments = response.getData().getDocuments().getDocumentsUploaded();
        assertThat(actualDocuments.size()).isEqualTo(2);
        assertThat(actualDocuments.get(0).getValue()).isSameAs(doc1.getValue());
        assertThat(actualDocuments.get(1).getValue()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldSortDocumentsInDescendingOrderAndAddNewDocumentsToTopOfList() {
        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();

        final ListValue<DivorceDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "d9d.pdf", D9D);

        final ListValue<DivorceDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004", "bailiff.pdf", BAILIFF_SERVICE);

        final CaseData previousCaseData = caseData();
        previousCaseData.getDocuments().setDocumentsUploaded(List.of(doc1, doc2));

        previousCaseDetails.setData(previousCaseData);

        final ListValue<DivorceDocument> doc3 =
            getDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005", "aos.pdf", ACKNOWLEDGEMENT_OF_SERVICE);

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getDocuments().setDocumentsUploaded(List.of(doc1, doc2, doc3));

        updatedCaseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerUploadDocument.aboutToSubmit(updatedCaseDetails, previousCaseDetails);

        List<ListValue<DivorceDocument>> actualDocuments = response.getData().getDocuments().getDocumentsUploaded();
        assertThat(actualDocuments.size()).isEqualTo(3);
        assertThat(actualDocuments.get(0).getValue()).isSameAs(doc3.getValue());
        assertThat(actualDocuments.get(1).getValue()).isSameAs(doc1.getValue());
        assertThat(actualDocuments.get(2).getValue()).isSameAs(doc2.getValue());
    }

    private ListValue<DivorceDocument> getDocumentListValue(
        String url,
        String filename,
        DocumentType documentType
    ) {
        return ListValue.<DivorceDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(DivorceDocument.builder()
                .documentType(documentType)
                .documentLink(Document
                    .builder()
                    .url(url)
                    .filename(filename)
                    .binaryUrl(url + "/binary")
                    .build()
                )
                .build())
            .build();
    }
}
