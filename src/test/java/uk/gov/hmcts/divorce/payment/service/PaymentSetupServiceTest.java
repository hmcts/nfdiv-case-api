package uk.gov.hmcts.divorce.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_DIVORCE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.payment.service.PaymentSetupService.getPaymentCallbackUrl;
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

        verify(paymentService, never()).createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
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

        when(paymentService.createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary))
            .thenReturn(TEST_SERVICE_REFERENCE);

        String response = paymentSetupService.createApplicationFeeServiceRequest(caseData, TEST_CASE_ID);

        verify(paymentService).createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
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

        verify(paymentService, never()).createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
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

        when(paymentService.createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary))
            .thenReturn(TEST_SERVICE_REFERENCE);

        String response = paymentSetupService.createFinalOrderFeeServiceRequest(caseData, TEST_CASE_ID, orderSummary);

        verify(paymentService).createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldNotCreateServiceFeeOrderSummaryIfItAlreadyExists() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setAlternativeService(
            AlternativeService.builder()
                .servicePaymentFee(
                    FeeDetails.builder()
                        .orderSummary(orderSummary)
                        .build()
                )
                .build()
        );

        OrderSummary response = paymentSetupService.createServiceApplicationOrderSummary(caseData.getAlternativeService(), TEST_CASE_ID);

        verify(paymentService, never()).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_WITHOUT_NOTICE);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldCreateServiceApplicationFeeOrderSummaryIfDoesNotAlreadyExist() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setAlternativeService(AlternativeService.builder().build());

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_WITHOUT_NOTICE))
            .thenReturn(orderSummary);

        OrderSummary response = paymentSetupService.createServiceApplicationOrderSummary(
            caseData.getAlternativeService(), TEST_CASE_ID
        );

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_WITHOUT_NOTICE);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldUseBailiffFeeServiceApplicationTypeIsBailiff() {
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setAlternativeService(
            AlternativeService.builder()
                .alternativeServiceType(AlternativeServiceType.BAILIFF)
                .build()
        );

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF))
            .thenReturn(orderSummary);

        OrderSummary response = paymentSetupService.createServiceApplicationOrderSummary(
            caseData.getAlternativeService(), TEST_CASE_ID
        );

        verify(paymentService).getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF);
        assertThat(response).isEqualTo(orderSummary);
    }

    @Test
    void shouldNotCreateServiceApplicationFeeServiceRequestIfItAlreadyExists() {
        final CaseData caseData = new CaseData();
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setAlternativeService(
            AlternativeService.builder()
                .servicePaymentFee(
                    FeeDetails.builder()
                        .serviceRequestReference(TEST_SERVICE_REFERENCE)
                        .build()
                )
                .build()
        );

        String response = paymentSetupService.createServiceApplicationPaymentServiceRequest(
            caseData.getAlternativeService(), TEST_CASE_ID, TEST_FIRST_NAME
        );

        verify(paymentService, never()).createServiceRequestReference(getPaymentCallbackUrl(), TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void shouldCreateServiceApplicationFeeServiceRequestIfDoesNotAlreadyExist() {
        final CaseData caseData = new CaseData();
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        final OrderSummary orderSummary = OrderSummary.builder().build();
        caseData.setAlternativeService(
            AlternativeService.builder()
                .servicePaymentFee(
                    FeeDetails.builder()
                        .orderSummary(orderSummary)
                        .build()
                )
                .build()
        );

        when(paymentService.createServiceRequestReference(null, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary))
            .thenReturn(TEST_SERVICE_REFERENCE);

        String response = paymentSetupService.createServiceApplicationPaymentServiceRequest(
            caseData.getAlternativeService(), TEST_CASE_ID, TEST_FIRST_NAME
        );

        verify(paymentService).createServiceRequestReference(null, TEST_CASE_ID, TEST_FIRST_NAME, orderSummary);
        assertThat(response).isEqualTo(TEST_SERVICE_REFERENCE);
    }
}
