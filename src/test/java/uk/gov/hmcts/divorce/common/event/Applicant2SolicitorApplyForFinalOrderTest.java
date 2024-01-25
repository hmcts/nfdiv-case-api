package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.notification.Applicant2SolicitorAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.service.ApplyForFinalOrderService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.solicitor.event.page.SolFinalOrderPayment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant2SolicitorApplyForFinalOrder.FINAL_ORDER_REQUESTED_APP2_SOL;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getPbaNumbersForAccount;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(MockitoExtension.class)
class Applicant2SolicitorApplyForFinalOrderTest {

    private static final String PBA_NUMBER = "PBA0012345";
    private static final String FEE_ACCOUNT_REF = "REF01";

    private static final OrderSummary orderSummary = OrderSummary
        .builder()
        .paymentTotal("16700")
        .fees(singletonList(getFeeListValue()))
        .build();

    @Mock
    private Applicant2SolicitorAppliedForFinalOrderNotification applicant2SolicitorAppliedForFinalOrderNotification;

    @Mock
    private SolFinalOrderPayment solFinalOrderPayment;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private Applicant2SolicitorApplyForFinalOrder applicant2SolicitorApplyForFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorApplyForFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(FINAL_ORDER_REQUESTED_APP2_SOL);
    }

    @Test
    void shouldSetOrderSummaryAndSolicitorFeesInPoundsAndSolicitorRolesAndPbaNumbersWhenAboutToStartIsInvoked() {

        final long caseId = TEST_CASE_ID;
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE)).thenReturn(orderSummary);
        when(orderSummary.getPaymentTotal()).thenReturn("16700");

        var midEventCaseData = caseData();
        midEventCaseData.getApplication().setPbaNumbers(getPbaNumbersForAccount("PBA0012345"));

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorApplyForFinalOrder.aboutToStart(caseDetails);

        assertThat(response.getData().getFinalOrder().getApplicant2SolFinalOrderFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getFinalOrder().getApplicant2SolFinalOrderFeeInPounds()).isEqualTo("167");
    }

    @Test
    void shouldAddFinalOrderPaymentIfPaymentsExists() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .created(LocalDateTime.now())
            .feeCode("FEE0227")
            .reference(orderSummary.getPaymentReference())
            .status(PaymentStatus.SUCCESS)
            .transactionId("Transaction1")
            .build();
        final ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .id(UUID.randomUUID().toString())
            .value(payment)
            .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getFinalOrder().setFinalOrderPayments(payments);
        caseData.getFinalOrder().setApplicant2SolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        caseData.getFinalOrder().setFinalOrderPbaNumbers(
            DynamicList.builder()
                .value(DynamicListElement.builder().label(PBA_NUMBER).build())
                .build()
        );
        caseData.getFinalOrder().setApplicant2SolFinalOrderFeeAccountReference(FEE_ACCOUNT_REF);
        caseData.getFinalOrder().setApplicant2FinalOrderStatementOfTruth(YES);
        caseData.getFinalOrder().setApplicant2SolFinalOrderFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        PbaResponse pbaResponse = new PbaResponse(CREATED, null, "1234");
        when(paymentService.processPbaPayment(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2().getSolicitor(),
            PBA_NUMBER,
            orderSummary,
            FEE_ACCOUNT_REF
        ))
            .thenReturn(pbaResponse);

        when(applyForFinalOrderService.applyForFinalOrderAsApplicant2Sol(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorApplyForFinalOrder.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).isNull();
        assertThat(response.getData().getFinalOrder().getFinalOrderPayments()).hasSize(2);
    }

    @Test
    void shouldReturnErrorIfFinalOrderPaymentIfPaymentFails() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build());
        caseData.getFinalOrder().setApplicant2SolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        caseData.getFinalOrder().setFinalOrderPbaNumbers(
            DynamicList.builder()
                .value(DynamicListElement.builder().label(PBA_NUMBER).build())
                .build()
        );
        caseData.getFinalOrder().setApplicant2SolFinalOrderFeeAccountReference(FEE_ACCOUNT_REF);
        caseData.getFinalOrder().setApplicant2FinalOrderStatementOfTruth(YES);
        caseData.getFinalOrder().setApplicant2SolFinalOrderFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        PbaResponse pbaResponse = new PbaResponse(HttpStatus.BAD_REQUEST, "Payment Failed", "1234");
        when(paymentService.processPbaPayment(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2().getSolicitor(),
            PBA_NUMBER,
            orderSummary,
            FEE_ACCOUNT_REF
        ))
            .thenReturn(pbaResponse);

        when(applyForFinalOrderService.applyForFinalOrderAsApplicant2Sol(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorApplyForFinalOrder.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).contains("Payment Failed");
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorIfFinalOrderPaymentMissingPbaNumber() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.getFinalOrder().setApplicant2SolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(applyForFinalOrderService.applyForFinalOrderAsApplicant2Sol(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorApplyForFinalOrder.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getErrors()).contains("PBA number not present when payment method is 'Solicitor fee account (PBA)'");
        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldSendApp2AppliedForFinalOrderNotifications() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();
        caseDetails.setState(RespondentFinalOrderRequested);

        applicant2SolicitorApplyForFinalOrder.submitted(caseDetails, null);

        verify(notificationDispatcher).send(applicant2SolicitorAppliedForFinalOrderNotification, caseData, caseDetails.getId());
    }
}
