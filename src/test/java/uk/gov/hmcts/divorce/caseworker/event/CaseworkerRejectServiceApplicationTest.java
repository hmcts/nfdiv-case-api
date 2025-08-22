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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRejectServiceApplication.CASEWORKER_REJECT_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class CaseworkerRejectServiceApplicationTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private CaseworkerRejectServiceApplication caseworkerRejectServiceApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRejectServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REJECT_SERVICE_APPLICATION);
    }

    @Test
    void shouldReturnErrorWhenRejectingServiceApplicationWhenNoServiceApplicationExists() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(null);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains("No service application to reject.");
    }

    @Test
    void shouldReturnErrorWhenRejectingServiceApplicationNotInitiatedByCitizen() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(AlternativeService.builder()
            .serviceApplicationSubmittedOnline(YesOrNo.NO)
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).contains(
                "Active service application cannot be rejected since it hasn't been submitted online.");
    }

    @Test
    void shouldDeleteServiceApplicationAnswersDocumentIfPresent() {
        Document doc = Document.builder()
            .binaryUrl("test.pdf")
            .build();

        DivorceDocument divorceDocument = DivorceDocument.builder()
            .documentLink(doc)
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(AlternativeService.builder()
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .serviceApplicationAnswers(divorceDocument)
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        verify(documentRemovalService).deleteDocument(doc);
    }

    @Test
    void shouldDeleteServiceApplicationDocuments() {
        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "test.pdf",
            DocumentType.OTHER
        );

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "test.pdf",
            DocumentType.OTHER
        );

        List<ListValue<DivorceDocument>> divorceDocuments = List.of(doc1, doc2);

        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(AlternativeService.builder()
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .serviceApplicationDocuments(divorceDocuments)
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        verify(documentRemovalService).deleteDocument(divorceDocuments);
    }

    @Test
    void shouldDeleteServiceApplicationWithoutAnySupportingDocuments() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(AlternativeService.builder()
                .serviceApplicationSubmittedOnline(YesOrNo.YES)
                .serviceApplicationDocuments(null)
                .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
                .id(TEST_CASE_ID)
                .data(caseData)
                .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
                caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
        verifyNoInteractions(documentRemovalService);
    }

    @Test
    void shouldSetCaseStateToAwaitingAos() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(AlternativeService.builder()
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .state(AwaitingServiceConsideration)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldResetAlternativeService() {

        final CaseData caseData = CaseData.builder().build();
        caseData.setAlternativeService(AlternativeService.builder()
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .state(AwaitingServiceConsideration)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRejectServiceApplication.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getAlternativeService().getServiceApplicationSubmittedOnline()).isNull();
    }
}
