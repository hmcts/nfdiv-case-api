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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUploadServiceApplicationDocuments.UPLOAD_SERVICE_APPLICATION_DOCS;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerUploadServiceApplicationDocumentsTest {

    @InjectMocks
    private CaseworkerUploadServiceApplicationDocuments caseworkerUploadServiceApplicationDocuments;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUploadServiceApplicationDocuments.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(UPLOAD_SERVICE_APPLICATION_DOCS);
    }

    @Test
    void shouldReturnErrorFromMidEventWhenNoNewDocumentsAreAddedAndNoAdditionalNotesProvided() {
        final ListValue<DivorceDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/1", "file1.pdf", OTHER);

        final ListValue<DivorceDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/2", "file2.pdf", OTHER);

        CaseData caseData = CaseData.builder().build();
        caseData.getAlternativeService().setServiceApplicationDocuments(List.of(doc1, doc2));
        caseData.getAlternativeService().setAlternativeServiceJudgeOrLegalAdvisorDetails(null);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadServiceApplicationDocuments
            .midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly("Please upload supporting documents and/or provide further"
            + " details for Judge or Legal Advisor.");
    }

    @Test
    void shouldNotReturnErrorFromMidEventIfAtLeastOneNewDocumentIsAdded() {
        final ListValue<DivorceDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/1", "file1.pdf", OTHER);

        final ListValue<DivorceDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/2", "file2.pdf", OTHER);

        CaseData beforeCaseData = CaseData.builder().build();
        beforeCaseData.getAlternativeService().setServiceApplicationDocuments(List.of(doc1));
        beforeCaseData.getAlternativeService().setAlternativeServiceJudgeOrLegalAdvisorDetails(null);

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        beforeCaseDetails.setData(beforeCaseData);
        beforeCaseDetails.setId(TEST_CASE_ID);

        CaseData caseData = CaseData.builder().build();
        caseData.getAlternativeService().setServiceApplicationDocuments(List.of(doc1, doc2));
        caseData.getAlternativeService().setAlternativeServiceJudgeOrLegalAdvisorDetails(null);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadServiceApplicationDocuments
            .midEvent(caseDetails, beforeCaseDetails);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldNotReturnErrorFromMidEventIfAdditionalDetailsIsProvided() {
        final ListValue<DivorceDocument> doc1 =
            getDocumentListValue("http://localhost:4200/assets/1", "file1.pdf", OTHER);

        final ListValue<DivorceDocument> doc2 =
            getDocumentListValue("http://localhost:4200/assets/2", "file2.pdf", OTHER);

        CaseData beforeCaseData = CaseData.builder().build();
        beforeCaseData.getAlternativeService().setServiceApplicationDocuments(List.of(doc1, doc2));
        beforeCaseData.getAlternativeService().setAlternativeServiceJudgeOrLegalAdvisorDetails(null);

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        beforeCaseDetails.setData(beforeCaseData);
        beforeCaseDetails.setId(TEST_CASE_ID);

        CaseData caseData = CaseData.builder().build();
        caseData.getAlternativeService().setServiceApplicationDocuments(List.of(doc1, doc2));
        caseData.getAlternativeService().setAlternativeServiceJudgeOrLegalAdvisorDetails("Some details");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadServiceApplicationDocuments
            .midEvent(caseDetails, beforeCaseDetails);

        assertThat(response.getErrors()).isEmpty();
    }


    @Test
    void shouldUpdateStateToAwaitingServiceConsideration() {
        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(State.AwaitingDocuments);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadServiceApplicationDocuments
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingServiceConsideration);
    }

    @Test
    void shouldUpdateStateToAwaitingServicePayment() {
        CaseData caseData = CaseData.builder().build();
        caseData.getAlternativeService().getServicePaymentFee().setHelpWithFeesReferenceNumber("HWF123456");

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(State.AwaitingDocuments);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUploadServiceApplicationDocuments
            .aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingServicePayment);
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
