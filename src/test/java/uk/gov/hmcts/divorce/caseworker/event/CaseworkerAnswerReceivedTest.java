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
import uk.gov.hmcts.divorce.citizen.notification.DisputedApplicationAnswerReceivedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAnswerReceived.CASEWORKER_ADD_ANSWER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEF;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class CaseworkerAnswerReceivedTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private DisputedApplicationAnswerReceivedNotification answerReceivedNotification;

    @InjectMocks
    private CaseworkerAnswerReceived caseworkerAnswerReceived;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAnswerReceived.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ADD_ANSWER);
    }

    @Test
    void shouldFetchOrderSummaryInAboutToStart() {
        final var caseData = caseData();
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .id(TEST_CASE_ID)
            .createdDate(LOCAL_DATE_TIME)
            .build();

        caseworkerAnswerReceived.aboutToStart(caseDetails);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ISSUE, KEYWORD_DEF);
    }

    @Test
    void shouldAddD11DocumentToTheStartDocumentsUploadedList() {
        final var caseData = caseData();
        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);
        List<ListValue<DivorceDocument>> documentsUploaded = new ArrayList<>();
        documentsUploaded.add(doc1);
        caseData.getDocuments().setDocumentsUploaded(documentsUploaded);

        DivorceDocument d11 = DivorceDocument.builder()
            .documentDateAdded(LocalDate.now())
            .documentLink(
                Document.builder().url("http://localhost:4200/assets/d11").filename("d11.pdf").binaryUrl("d11.pdf/binary").build()
            )
            .documentType(DocumentType.D11)
            .build();
        caseData.getDocuments().setAnswerReceivedSupportingDocuments(
            List.of(
                ListValue.<DivorceDocument>builder()
                    .value(d11)
                    .build()
            )
        );

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .state(Holding)
            .data(caseData)
            .id(TEST_CASE_ID)
            .createdDate(LOCAL_DATE_TIME)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAnswerReceived.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocuments().getDocumentsUploaded().size()).isEqualTo(2);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().get(0).getValue()).isSameAs(d11);
    }

    @Test
    void shouldTransitionStateToAwaitingJsNullityFromAwaitingAnswer() {
        CaseData caseData = caseData();
        caseData.getDocuments().setAnswerReceivedSupportingDocuments(new ArrayList<>());
        final CaseDetails<CaseData, State> caseDetails =
            CaseDetails.<CaseData, State>builder().data(caseData).state(AwaitingAnswer).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAnswerReceived.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingJsNullity);
    }

    @Test
    void shouldKeepExistingStateWhenNotAwaitingAnswer() {
        CaseData caseData = caseData();
        caseData.getDocuments().setAnswerReceivedSupportingDocuments(new ArrayList<>());
        final CaseDetails<CaseData, State> caseDetails =
            CaseDetails.<CaseData, State>builder().data(caseData).state(Holding).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAnswerReceived.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldCreateDocumentsListAndAddD11DocumentIfDocumentsUploadedIsNull() {
        final var caseData = caseData();

        DivorceDocument d11 = DivorceDocument.builder()
            .documentDateAdded(LocalDate.now())
            .documentLink(
                Document
                    .builder()
                    .url("http://localhost:4200/assets/d11")
                    .filename("d11.pdf")
                    .binaryUrl("d11.pdf/binary")
                    .build()
            )
            .documentType(DocumentType.D11)
            .build();
        caseData.getDocuments().setAnswerReceivedSupportingDocuments(
            List.of(
                ListValue.<DivorceDocument>builder()
                    .value(d11)
                    .build()
            )
        );

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .state(Holding)
            .data(caseData)
            .id(TEST_CASE_ID)
            .createdDate(LOCAL_DATE_TIME)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAnswerReceived.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocuments().getDocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getDocuments().getDocumentsUploaded().get(0).getValue()).isSameAs(d11);
    }

    @Test
    void shouldSendNotificationOnSubmittedIfApplicationDisputed() {
        final var caseData = caseData();
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder().howToRespondApplication(HowToRespondApplication.DISPUTE_DIVORCE).build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .id(TEST_CASE_ID)
            .build();

        caseworkerAnswerReceived.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(answerReceivedNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldNotSendNotificationOnSubmittedIfApplicationNotDisputed() {
        final var caseData = caseData();
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder().howToRespondApplication(HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE).build()
        );

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .id(TEST_CASE_ID)
            .build();

        caseworkerAnswerReceived.submitted(caseDetails, caseDetails);

        verifyNoInteractions(notificationDispatcher);
    }
}
