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
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAnswerReceived.CASEWORKER_ADD_ANSWER;
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
public class CaseworkerAnswerReceivedTest {

    @Mock
    private PaymentService paymentService;

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

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        caseworkerAnswerReceived.aboutToStart(caseDetails);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ISSUE, KEYWORD_DEF);
    }

    @Test
    void shouldAddD11DocumentToDocumentsUploaded() {
        final var caseData = caseData();
        final ListValue<DivorceDocument> doc1 =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);
        List<ListValue<DivorceDocument>> documentsUploaded = new ArrayList<>();
        documentsUploaded.add(doc1);
        caseData.setDocumentsUploaded(documentsUploaded);

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
        caseData.setD11Document(d11);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAnswerReceived.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocumentsUploaded().size()).isEqualTo(2);
        assertThat(response.getData().getDocumentsUploaded().get(1).getValue()).isSameAs(d11);
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
        caseData.setD11Document(d11);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerAnswerReceived.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocumentsUploaded().size()).isEqualTo(1);
        assertThat(response.getData().getDocumentsUploaded().get(0).getValue()).isSameAs(d11);
    }
}
