package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.task.SolicitorSubmitNotification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitApplicationServiceTest {

    @Mock
    private MiniApplicationRemover miniApplicationRemover;

    @Mock
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @Mock
    private Clock clock;

    @InjectMocks
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

    @Test
    void shouldCompleteStepsToUpdateApplication() {

        final List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.getApplication().setApplicant1StatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(miniApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(solicitorSubmitNotification.apply(caseDetails)).thenReturn(caseDetails);

        final var response = solicitorSubmitApplicationService.aboutToSubmit(caseDetails);

        assertThat(response.getCaseData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetStateToAwaitingHWfDecisionWhenPaymentMethodIsHwf() {

        final List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.getApplication().setApplicant1StatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolPaymentHowToPay(SolicitorPaymentMethod.FEES_HELP_WITH);

        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(miniApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(solicitorSubmitNotification.apply(caseDetails)).thenReturn(caseDetails);

        final var response = solicitorSubmitApplicationService.aboutToSubmit(caseDetails);

        assertThat(response.getCaseData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(AwaitingHWFDecision);
    }

    @Test
    void shouldRemoveDraftApplicationAndNotifyApplicantAndSetStateToSubmittedForAboutToSubmit() {

        final List<ListValue<DivorceDocument>> generatedDocuments = singletonList(documentWithType(DIVORCE_APPLICATION));
        final CaseData caseData = CaseData.builder().build();
        caseData.setDocumentsGenerated(generatedDocuments);
        caseData.getApplication().setApplicant1StatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        final ListValue<Payment> payment = new ListValue<>(null, Payment
            .builder()
            .amount(55000)
            .channel("online")
            .feeCode("FEE0001")
            .reference("paymentRef")
            .status(PaymentStatus.SUCCESS)
            .transactionId("ge7po9h5bhbtbd466424src9tk")
            .build());

        caseData.getApplication().setApplicationPayments(singletonList(payment));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(miniApplicationRemover.apply(caseDetails)).thenReturn(caseDetails);
        when(solicitorSubmitNotification.apply(caseDetails)).thenReturn(caseDetails);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("Etc/UTC"));

        final var response = solicitorSubmitApplicationService.aboutToSubmit(caseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
        assertThat(response.getCaseData().getApplication().getDateSubmitted()).isEqualTo(LocalDateTime.now(clock));
    }
}
