package uk.gov.hmcts.divorce.caseworker.event;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationOfflineResponseSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList.RFI_DOCUMENT_REMOVED_NOTICE;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.REQUEST_FOR_INFORMATION_RESPONSE_DOC;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_TEXT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addOfflineResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.addResponseToLatestRequestForInformation;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getRequestForInformationCaseDetails;
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

        verify(documentRemovalService).handleDeletionOfScannedDocuments(beforeCaseData, currentCaseData);
    }

    @Test
    void shouldRemoveDocumentFromCurrentGeneralApplication() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "general_application.pdf",
            GENERAL_APPLICATION
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        GeneralApplication generalApplication =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2))
                .build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc2)).build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments()).isEmpty();
    }

    @Test
    void shouldRemoveNoDocumentFromCurrentGeneralApplication() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "general_application.pdf",
            GENERAL_APPLICATION
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        GeneralApplication generalApplication =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2))
                .build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1)).build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments()).isNotEmpty();
    }

    @Test
    void shouldRemoveRelevantDocumentFromCurrentGeneralApplication() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "general_application.pdf",
            GENERAL_APPLICATION
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        final ListValue<DivorceDocument> doc3 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130005",
            "general_application1.pdf",
            GENERAL_APPLICATION
        );

        GeneralApplication generalApplication =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1, doc3))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2, doc3))
                .build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc2, doc3)).build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments()).isNotEmpty();
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments().size()).isEqualTo(1);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments()
                .get(0).getValue().getDocumentLink().getFilename()).isEqualTo("general_application1.pdf");
    }

    @Test
    void removeDocumentFromGeneralApplicationCollection() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "general_application.pdf",
            GENERAL_APPLICATION
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        GeneralApplication generalApplication =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        ListValue<GeneralApplication> generalApplicationListValue1 = ListValue.<GeneralApplication>builder()
            .id(UUID.randomUUID().toString())
            .value(generalApplication)
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2))
                .build())
            .generalApplications(List.of(generalApplicationListValue1))
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc2)).build())
            .generalApplications(List.of(generalApplicationListValue1))
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        assertThat(response.getData().getGeneralApplications().get(0)
            .getValue().getGeneralApplicationDocuments()).isEmpty();
    }

    @Test
    void removeDocumentFromAllRelevantGeneralApplicationsInCollection() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "general_application.pdf",
            GENERAL_APPLICATION
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );

        GeneralApplication generalApplication1 =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        GeneralApplication generalApplication2 =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1, doc2))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        ListValue<GeneralApplication> generalApplicationListValue1 = ListValue.<GeneralApplication>builder()
            .id(UUID.randomUUID().toString())
            .value(generalApplication1)
            .build();

        ListValue<GeneralApplication> generalApplicationListValue2 = ListValue.<GeneralApplication>builder()
            .id(UUID.randomUUID().toString())
            .value(generalApplication2)
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2))
                .build())
            .generalApplications(List.of(generalApplicationListValue1, generalApplicationListValue2))
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc2)).build())
            .generalApplications(List.of(generalApplicationListValue1, generalApplicationListValue2))
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        assertThat(response.getData().getGeneralApplications().get(0)
            .getValue().getGeneralApplicationDocuments()).isEmpty();
        assertThat(response.getData().getGeneralApplications().get(1)
            .getValue().getGeneralApplicationDocuments()).isNotEmpty();
    }

    @Test
    void shouldRemoveNoDocumentFromCurrentGeneralApplicationWhenDocumentTypeNotSet() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "general_application.pdf",
            GENERAL_APPLICATION
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "co_application.pdf",
            CONDITIONAL_ORDER_APPLICATION
        );
        doc2.getValue().setDocumentType(null);

        GeneralApplication generalApplication =  GeneralApplication.builder()
            .generalApplicationDocuments(List.of(doc1))
            .generalApplicationType(GeneralApplicationType.DEEMED_SERVICE)
            .build();

        CaseData beforeCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1, doc2))
                .build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .data(beforeCaseData)
            .build();

        CaseData currentCaseData = CaseData.builder()
            .documents(CaseDocuments.builder()
                .documentsUploaded(List.of(doc1)).build())
            .generalApplication(generalApplication)
            .build();

        CaseDetails<CaseData, State> currentDetails = CaseDetails.<CaseData, State>builder()
            .data(currentCaseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        assertThat(response.getData().getGeneralApplication().getGeneralApplicationDocuments()).isNotEmpty();
    }

    @Test
    void shouldPopulateRfiResponseDocList() {
        final CaseDetails<CaseData, State> caseDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        addResponseToLatestRequestForInformation(caseDetails.getData(), caseDetails.getData().getApplicant1());
        addOfflineResponseToLatestRequestForInformation(caseDetails.getData(), RequestForInformationOfflineResponseSoleParties.APPLICANT);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRemoveDocument.aboutToStart(caseDetails);

        assertThat(response.getData().getRequestForInformationList().getRfiOnlineResponseDocuments().size()).isEqualTo(1);
    }

    @Test
    void shouldRemoveRfiResponseUploadedDocument() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "rfi_response_1.pdf",
            REQUEST_FOR_INFORMATION_RESPONSE_DOC
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "rfi_response_2.pdf",
            REQUEST_FOR_INFORMATION_RESPONSE_DOC
        );

        CaseDetails<CaseData, State> beforeDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        addOfflineResponseToLatestRequestForInformation(
            beforeDetails.getData(),
            RequestForInformationOfflineResponseSoleParties.APPLICANT,
            doc1.getValue()
        );
        addOfflineResponseToLatestRequestForInformation(
            beforeDetails.getData(),
            RequestForInformationOfflineResponseSoleParties.APPLICANT,
            doc2.getValue()
        );
        beforeDetails.getData().getDocuments().setDocumentsUploaded(List.of(doc1, doc2));

        CaseDetails<CaseData, State> currentDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        addOfflineResponseToLatestRequestForInformation(
            currentDetails.getData(),
            RequestForInformationOfflineResponseSoleParties.APPLICANT,
            doc1.getValue()
        );
        addOfflineResponseToLatestRequestForInformation(
            currentDetails.getData(),
            RequestForInformationOfflineResponseSoleParties.APPLICANT,
            doc2.getValue()
        );
        currentDetails.getData().getDocuments().setDocumentsUploaded(List.of(doc1));

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        RequestForInformation responseRfi = response.getData().getRequestForInformationList().getLatestRequest();
        List<ListValue<DivorceDocument>> responseDocsUploaded = response.getData().getDocuments().getDocumentsUploaded();

        verify(documentRemovalService).deleteDocument(List.of(doc2));
        assertThat(responseRfi.getLatestResponse().getRequestForInformationResponseDetails())
            .isEqualTo(RFI_DOCUMENT_REMOVED_NOTICE + "\n\n" + TEST_TEXT);
        assertThat(responseRfi.getLatestResponse().getRfiOfflineResponseDocs().size()).isEqualTo(0);
        assertThat(responseRfi.getResponseByIndex(1).getRfiOfflineResponseDocs().size()).isEqualTo(1);
        assertThat(responseDocsUploaded.size()).isEqualTo(1);
        assertThat(responseDocsUploaded.get(0).getValue()).isEqualTo(doc1.getValue());
    }

    @Test
    void shouldRemoveRfiResponseDocuments() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "rfi_response_1.pdf",
            REQUEST_FOR_INFORMATION_RESPONSE_DOC
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "rfi_response_2.pdf",
            REQUEST_FOR_INFORMATION_RESPONSE_DOC
        );

        CaseDetails<CaseData, State> beforeDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        addResponseToLatestRequestForInformation(
            beforeDetails.getData(),
            beforeDetails.getData().getApplicant1(),
            doc1.getValue()
        );
        beforeDetails.getData().getRequestForInformationList().getLatestRequest().getLatestResponse()
            .getRequestForInformationResponseDocs().add(doc2);
        beforeDetails.getData().getRequestForInformationList().getLatestRequest().getLatestResponse()
            .setRequestForInformationResponseDetails(null);
        beforeDetails.getData().getRequestForInformationList().buildResponseDocList();

        CaseDetails<CaseData, State> currentDetails = getRequestForInformationCaseDetails(APPLICANT, false, false);
        addResponseToLatestRequestForInformation(
            currentDetails.getData(),
            currentDetails.getData().getApplicant1(),
            doc1.getValue()
        );
        currentDetails.getData().getRequestForInformationList().getLatestRequest().getLatestResponse()
            .getRequestForInformationResponseDocs().add(doc2);
        currentDetails.getData().getRequestForInformationList().getLatestRequest().getLatestResponse()
                .setRequestForInformationResponseDetails(null);
        currentDetails.getData().getRequestForInformationList().buildResponseDocList();

        Optional<ListValue<DivorceDocument>> rfiResponseDoc =
            emptyIfNull(currentDetails.getData().getRequestForInformationList().getRfiOnlineResponseDocuments())
                .stream()
                .filter(rfiDoc -> rfiDoc.getValue().equals(doc2.getValue()))
                .findFirst();

        rfiResponseDoc.ifPresent(
            divorceDocumentListValue ->
                currentDetails.getData().getRequestForInformationList().getRfiOnlineResponseDocuments().remove(divorceDocumentListValue));

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRemoveDocument.aboutToSubmit(currentDetails, beforeDetails);
        RequestForInformation responseRfi = response.getData().getRequestForInformationList().getLatestRequest();
        List<ListValue<DivorceDocument>> responseDocs = response.getData().getRequestForInformationList().getRfiOnlineResponseDocuments();

        verify(documentRemovalService).deleteDocument(List.of(
            beforeDetails.getData().getRequestForInformationList().getRfiOnlineResponseDocuments().get(1))
        );
        assertThat(responseRfi.getLatestResponse().getRequestForInformationResponseDetails())
            .isEqualTo(RFI_DOCUMENT_REMOVED_NOTICE);
        assertThat(responseRfi.getLatestResponse().getRequestForInformationResponseDocs().size()).isEqualTo(1);
        assertThat(responseRfi.getLatestResponse().getRequestForInformationResponseDocs().get(0).getValue()).isEqualTo(doc1.getValue());
        assertThat(responseDocs).isNull();
    }
}
