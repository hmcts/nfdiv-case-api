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
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralEmail;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPrepareGeneralEmailAttachments.CASEWORKER_PREPARE_GENERAL_EMAIL;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPrepareGeneralEmailAttachments.MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceGeneralOrderListValue;

@ExtendWith(MockitoExtension.class)
public class CaseworkerPrepareGeneralEmailAttachmentsTest {
    @InjectMocks
    private CaseworkerPrepareGeneralEmailAttachments generalEmail;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalEmail.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .containsExactly(CASEWORKER_PREPARE_GENERAL_EMAIL);
    }

    @Test
    void shouldRemoveStaleGeneralEmailAttachmentDataInAboutToStart() {
        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail.builder()
                .generalEmailAttachments(List.of(
                    ListValue.<DivorceDocument>builder().value(
                        DivorceDocument.builder().documentFileName("dummy").build()
                    ).build()
                )).build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeneralEmailAttachments()).isNull();
    }

    @Test
    void shouldAddScannedDocumentsFromCaseDataToGeneralEmailScannedDocNamesInAboutToStart() {
        final CaseData caseData = caseData();

        caseData.getDocuments().setScannedDocuments(getListOfScannedDocument(1));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeScannedDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeScannedDocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldAddUploadedDocumentsFromCaseDataToGeneralEmailUploadedDocNamesInAboutToStart() {
        final CaseData caseData = caseData();

        caseData.getDocuments().setDocumentsUploaded(getListOfDivorceDocument(1));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeUploadedDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeUploadedDocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldHandleUploadedDocumentsWithoutFilesAttachedInAboutToStart() {
        final CaseData caseData = caseData();

        List<ListValue<DivorceDocument>> divorceDocuments = getListOfDivorceDocument(1);
        divorceDocuments.add(ListValue.<DivorceDocument>builder().value(
            DivorceDocument.builder().documentEmailContent("dummy content").build()
        ).build());
        caseData.getDocuments().setDocumentsUploaded(divorceDocuments);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeUploadedDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeUploadedDocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldHandleScannedDocumentsWithoutFilesAttachedInAboutToStart() {
        final CaseData caseData = caseData();

        List<ListValue<ScannedDocument>> scannedDocuments = getListOfScannedDocument(1);
        scannedDocuments.add(ListValue.<ScannedDocument>builder().value(
            ScannedDocument.builder().type(ScannedDocumentType.COVERSHEET).build()
        ).build());
        caseData.getDocuments().setScannedDocuments(scannedDocuments);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeScannedDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeScannedDocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldAddGeneratedDocumentsFromCaseDataToGeneralEmailGeneratedDocNamesInAboutToStart() {
        final CaseData caseData = caseData();

        caseData.getDocuments().setDocumentsGenerated(getListOfDivorceDocument(1));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeGeneratedDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeGeneratedDocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldAddApp1DocumentsFromCaseDataToGeneralEmailApp1DocNamesInAboutToStart() {
        final CaseData caseData = caseData();

        caseData.getDocuments().setApplicant1DocumentsUploaded(getListOfDivorceDocument(1));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeApplicant1DocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeApplicant1DocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldAddApp2DocumentsFromCaseDataToGeneralEmailApp2DocNamesInAboutToStart() {
        final CaseData caseData = caseData();

        caseData.getDocuments().setApplicant2DocumentsUploaded(getListOfDivorceDocument(1));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeApplicant2DocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeApplicant2DocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldAddGeneralOrderDocsFromCaseDataToGeneralEmailGenOrderDocNamesInAboutToStart() {
        final CaseData caseData = caseData();

        String documentUrl = "http://localhost:8080/4567";

        Document generalOrderDoc1 = new Document(
            documentUrl,
            "generalOrder2020-07-16 11:10:34.pdf",
            documentUrl + "/binary"
        );

        final List<ListValue<DivorceGeneralOrder>> generalOrders1 = new ArrayList<>();
        generalOrders1.add(getDivorceGeneralOrderListValue(generalOrderDoc1, UUID.randomUUID().toString()));
        caseData.setGeneralOrders(generalOrders1);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(caseDetails);

        assertThat(response.getData().getGeneralEmail().getGeGeneralOrderDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeGeneralOrderDocumentNames()
            .getListItems().size()).isEqualTo(1);
    }

    @Test
    void shouldReturnErrorIfDocumentLinkNotProvidedGeneralEmailAttachments() {
        ListValue<DivorceDocument> generalEmailAttachment = new ListValue<>(
            "1",
            DivorceDocument
                .builder()
                .build()
        );
        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(singletonList(generalEmailAttachment))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please ensure all General Email attachments have been uploaded before continuing");
    }

    @Test
    void shouldReturnErrorIfAttachmentsExceedMaxAllowed() {

        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(11))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(String.format(
            "Number of attachments on General Email cannot exceed %s",MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS));
    }

    @Test
    void shouldReturnErrorIfSelectedDocumentsExceedMaxAllowed() {

        final CaseData caseData = caseData();
        caseData.getDocuments().setApplicant2DocumentsUploaded(getListOfDivorceDocument(11));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToStart(details);

        final CaseData updatedData = response.getData();

        List<DynamicListElement> selected = new ArrayList<>(updatedData.getGeneralEmail().getGeApplicant2DocumentNames().getListItems());
        updatedData.getGeneralEmail().getGeApplicant2DocumentNames().setValue(selected);

        updatedData.getGeneralEmail().setGeGeneratedDocumentNames(null);
        updatedData.getGeneralEmail().setGeUploadedDocumentNames(null);
        updatedData.getGeneralEmail().setGeScannedDocumentNames(null);
        updatedData.getGeneralEmail().setGeApplicant1DocumentNames(null);
        updatedData.getGeneralEmail().setGeGeneralOrderDocumentNames(null);


        final CaseDetails<CaseData, State> updatedDetails = new CaseDetails<>();
        updatedDetails.setData(updatedData);

        response = generalEmail.midEvent(updatedDetails, updatedDetails);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(String.format(
            "Number of attachments on General Email cannot exceed %s",MAX_NUMBER_GENERAL_EMAIL_ATTACHMENTS));
    }

    @Test
    void shouldAddAttachedDocumentNamesToGeneralEmailInAboutToSubmit() {
        final CaseData caseData = caseData();

        caseData.setGeneralEmail(
            GeneralEmail
                .builder()
                .generalEmailParties(APPLICANT)
                .generalEmailDetails("some details")
                .generalEmailAttachments(getListOfDivorceDocument(5))
                .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalEmail.aboutToSubmit(details, details);

        assertThat(response.getData().getGeneralEmail().getGeAttachedDocumentNames()).isNotNull();
        assertThat(response.getData().getGeneralEmail().getGeAttachedDocumentNames().getListItems().size())
            .isEqualTo(5);
        assertThat(response.getData().getGeneralEmail().getGeAttachedDocumentNames().getValue().size())
            .isEqualTo(5);
    }

    List<ListValue<DivorceDocument>> getListOfDivorceDocument(int size) {
        List<ListValue<DivorceDocument>> docList = new ArrayList<>();
        while (size > 0) {
            ListValue<DivorceDocument> documentListValue = new ListValue<>(
                UUID.randomUUID().toString(),
                DivorceDocument
                    .builder()
                    .documentLink(Document.builder().filename("dummy.file").build())
                    .build()
            );
            docList.add(documentListValue);
            size--;
        }
        return docList;
    }

    List<ListValue<ScannedDocument>> getListOfScannedDocument(int size) {
        List<ListValue<ScannedDocument>> docList = new ArrayList<>();
        while (size > 0) {
            ListValue<ScannedDocument> documentListValue = new ListValue<>(
                UUID.randomUUID().toString(),
                ScannedDocument
                    .builder()
                    .url(Document.builder().filename("dummy.file").build())
                    .build()
            );
            docList.add(documentListValue);
            size--;
        }
        return docList;
    }

    private User getCaseworkerUser() {
        var userDetails = UserInfo
            .builder()
            .givenName("testFname")
            .familyName("testSname")
            .name("testFname testSname")
            .build();

        return new User(TEST_AUTHORIZATION_TOKEN, userDetails);
    }
}
