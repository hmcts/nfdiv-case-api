package uk.gov.hmcts.divorce.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.payment.PaymentSetupService.PAYMENT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class PaymentSetupServiceTest {
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentSetupService paymentSetupService;

    @Test
    void shouldNotCreateApplicationFeeOrderSummaryIfItAlreadyExists() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setApplication(
            Application.builder()
                .applicationFeeOrderSummary(orderSummary)
                .build()
        );

        OrderSummary response = paymentSetupService.createApplicationFeeOrderSummary(caseData, TEST_CASE_ID);

        verify(paymentService, never()).getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE,KEYWORD_DIVORCE);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldCreateApplicationFeeOrderSummaryIfDoesNotAlreadyExist() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setApplication(Application.builder().build());

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE,KEYWORD_DIVORCE))
            .thenReturn(orderSummary);

        OrderSummary response = paymentSetupService.createApplicationFeeOrderSummary(caseData, TEST_CASE_ID);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_DIVORCE, EVENT_ISSUE,KEYWORD_DIVORCE);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldNotCreateApplicationFeeServiceRequestIfItAlreadyExists() {
        final CaseData caseData = new CaseData();
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setApplication(
            Application.builder()
                .applicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .applicationFeeOrderSummary(orderSummary)
                .build()
        );

        String response = paymentSetupService.createApplicationFeeServiceRequest(caseData, TEST_CASE_ID);

        verify(paymentService, never()).createServiceRequestReference(PAYMENT_CALLBACK_URL, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldCreateApplicationFeeServiceRequestIfDoesNotAlreadyExist() {
        final CaseData caseData = new CaseData();
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setApplication(
            Application.builder()
                .applicationFeeOrderSummary(orderSummary)
                .build()
        );

        when(paymentService.createServiceRequestReference(PAYMENT_CALLBACK_URL, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary))
            .thenReturn(TEST_SERVICE_REFERENCE);

        String response = paymentSetupService.createApplicationFeeServiceRequest(caseData, TEST_CASE_ID);

        verify(paymentService).createServiceRequestReference(PAYMENT_CALLBACK_URL, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldNotCreateFinalOrderFeeOrderSummaryIfItAlreadyExists() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .applicant2FinalOrderFeeOrderSummary(orderSummary)
                .build()
        );

        OrderSummary response = paymentSetupService.createFinalOrderFeeOrderSummary(caseData, TEST_CASE_ID);

        verify(paymentService, never()).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldCreateFinalOrderFeeOrderSummaryIfDoesNotAlreadyExist() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setFinalOrder(FinalOrder.builder().build());

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE))
            .thenReturn(orderSummary);

        OrderSummary response = paymentSetupService.createFinalOrderFeeOrderSummary(caseData, TEST_CASE_ID);

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldNotCreateFinalOrderFeeServiceRequestIfItAlreadyExists() {
        final CaseData caseData = new CaseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .applicant2FinalOrderFeeServiceRequestReference(TEST_SERVICE_REFERENCE)
                .applicant2FinalOrderFeeOrderSummary(orderSummary)
                .build()
        );

        String response = paymentSetupService.createFinalOrderFeeServiceRequest(caseData, TEST_CASE_ID, orderSummary);

        verify(paymentService, never()).createServiceRequestReference(PAYMENT_CALLBACK_URL, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldCreateFinalOrderFeeServiceRequestIfDoesNotAlreadyExist() {
        final CaseData caseData = new CaseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setFinalOrder(
            FinalOrder.builder()
                .applicant2FinalOrderFeeOrderSummary(orderSummary)
                .build()
        );

        when(paymentService.createServiceRequestReference(PAYMENT_CALLBACK_URL, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary))
            .thenReturn(TEST_SERVICE_REFERENCE);

        String response = paymentSetupService.createFinalOrderFeeServiceRequest(caseData, TEST_CASE_ID, orderSummary);

        verify(paymentService).createServiceRequestReference(PAYMENT_CALLBACK_URL, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }
}
