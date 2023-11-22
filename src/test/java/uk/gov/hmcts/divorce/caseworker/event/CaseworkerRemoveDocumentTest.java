package uk.gov.hmcts.divorce.caseworker.event;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.scannedDocumentWithType;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRemoveDocumentTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private CaseworkerRemoveDocument caseworkerRemoveDocument;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRemoveDocument.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CaseworkerRemoveDocument.CASEWORKER_REMOVE_DOCUMENT);
    }

    @Test
    void shouldRemoveApplicant1UploadedDocument() {

        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                "co_granted.pdf",
                CONDITIONAL_ORDER_GRANTED
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
                "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
                "co_application.pdf",
                CONDITIONAL_ORDER_APPLICATION
        );

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .applicant1DocumentsUploaded(List.of(doc1, doc2))
                .build())
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .applicant1DocumentsUploaded(List.of(doc1)).build())
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);

        verify(documentRemovalService).deleteDocument(List.of(doc2));
    }

    @Test
    void shouldRemoveApplicant2UploadedDocument() {

        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "co_granted.pdf",
            CONDITIONAL_ORDER_GRANTED
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .applicant2DocumentsUploaded(List.of(doc1, doc2))
                .build())
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .applicant2DocumentsUploaded(List.of(doc1)).build())
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);

        verify(documentRemovalService).deleteDocument(List.of(doc2));
    }

    @Test
    void shouldRemoveDocumentsGeneratedDocument() {

        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "co_granted.pdf",
            CONDITIONAL_ORDER_GRANTED
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsGenerated(List.of(doc1, doc2))
                .build())
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsGenerated(List.of(doc1)).build())
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);

        verify(documentRemovalService).deleteDocument(List.of(doc2));
    }

    @Test
    void shouldRemoveDocumentsUploadedDocument() {

        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "co_granted.pdf",
            CONDITIONAL_ORDER_GRANTED
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2))
                .build())
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1)).build())
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);

        verify(documentRemovalService).deleteDocument(List.of(doc2));
    }

    @Test
    void shouldSkipRemovalIfDocumentsListIsNull() {

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder().build())
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder().build())
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);

        verifyNoInteractions(documentRemovalService);
    }

    @Test
    void shouldRemoveScannedDocument() {

        final ListValue<ScannedDocument> doc1 = scannedDocumentWithType(
            FORM,
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003"
        );

        final ListValue<ScannedDocument> doc2 = scannedDocumentWithType(
            FORM,
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004"
        );

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .scannedDocuments(List.of(doc1, doc2))
                .build())
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .scannedDocuments(List.of(doc1)).build())
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);

        verify(documentRemovalService).deleteScannedDocuments(List.of(doc2));
    }
}
