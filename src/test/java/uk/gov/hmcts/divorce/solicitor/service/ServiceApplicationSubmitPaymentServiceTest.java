package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class ServiceApplicationSubmitPaymentServiceTest {

    @InjectMocks
    private ServiceApplicationSubmitPaymentService serviceApplicationSubmitPaymentService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Clock clock;

    @Test
    void shouldReturnNoErrorWhenPaymentMethodIsNotPba() {
        CaseData caseData = caseDataWithPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);

        Optional<String> result = serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldReturnErrorWhenPbaNumberIsMissingForPbaPayment() {
        CaseData caseData = caseDataWithPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        caseData.getAlternativeService().getServicePaymentFee().setOrderSummary(OrderSummary.builder().build());
        caseData.getAlternativeService().getServicePaymentFee().setServiceRequestReference(TEST_SERVICE_REFERENCE);

        Optional<String> result = serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData);

        assertThat(result).contains("PBA number not present when payment method is 'Solicitor fee account (PBA)'");
        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldReturnErrorWhenServiceRequestReferenceIsMissingForPbaPayment() {
        CaseData caseData = caseDataWithPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        caseData.getAlternativeService().getServicePaymentFee().setOrderSummary(serviceOrderSummary());
        caseData.getAlternativeService().getServicePaymentFee().setPbaNumbers(pbaNumbers());

        Optional<String> result = serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData);

        assertThat(result).contains("Service request reference is missing for PBA payment");
        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldReturnGenericErrorWhenPbaServiceFailsWithoutErrorMessage() {
        CaseData caseData = caseDataWithPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        caseData.getAlternativeService().getServicePaymentFee().setOrderSummary(serviceOrderSummary());
        caseData.getAlternativeService().getServicePaymentFee().setPbaNumbers(pbaNumbers());
        caseData.getAlternativeService().getServicePaymentFee().setServiceRequestReference(TEST_SERVICE_REFERENCE);

        when(paymentService.processPbaPayment(
            eq(TEST_CASE_ID),
            eq(TEST_SERVICE_REFERENCE),
            isNull(),
            eq("PBA0088776"),
            any(OrderSummary.class),
            isNull()
        )).thenReturn(new PbaResponse(BAD_REQUEST, null, null));

        Optional<String> result = serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData);

        assertThat(result).contains("Failed to process PBA payment");
    }

    @Test
    void shouldStorePaymentDetailsWhenPbaPaymentSucceeds() {
        setMockClock(clock);

        CaseData caseData = caseDataWithPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);
        caseData.getAlternativeService().getServicePaymentFee().setOrderSummary(serviceOrderSummary());
        caseData.getAlternativeService().getServicePaymentFee().setPbaNumbers(pbaNumbers());
        caseData.getAlternativeService().getServicePaymentFee().setServiceRequestReference(TEST_SERVICE_REFERENCE);

        when(paymentService.processPbaPayment(
            eq(TEST_CASE_ID),
            eq(TEST_SERVICE_REFERENCE),
            isNull(),
            eq("PBA0088776"),
            any(OrderSummary.class),
            isNull()
        )).thenReturn(new PbaResponse(CREATED, null, "RC-12345"));

        Optional<String> result = serviceApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData);

        assertThat(result).isEmpty();
        assertThat(caseData.getAlternativeService().getServicePaymentFee().getPaymentReference()).isEqualTo("RC-12345");
        assertThat(caseData.getAlternativeService().getServicePaymentFee().getHasCompletedOnlinePayment()).isEqualTo(YesOrNo.YES);
        assertThat(caseData.getAlternativeService().getServicePaymentFee().getDateOfPayment()).isEqualTo(getExpectedLocalDate());
    }

    private CaseData caseDataWithPaymentMethod(ServicePaymentMethod paymentMethod) {
        AlternativeService alternativeService = AlternativeService.builder().build();
        alternativeService.getServicePaymentFee().setPaymentMethod(paymentMethod);

        return CaseData.builder()
            .applicant1(Applicant.builder().build())
            .alternativeService(alternativeService)
            .build();
    }

    private DynamicList pbaNumbers() {
        return DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0088776").build())
            .build();
    }

    private OrderSummary serviceOrderSummary() {
        Fee fee = Fee.builder().code("FEE0423").build();
        ListValue<Fee> feeListValue = ListValue.<Fee>builder()
            .id(UUID.randomUUID().toString())
            .value(fee)
            .build();

        return OrderSummary.builder()
            .paymentTotal("23200")
            .fees(List.of(feeListValue))
            .build();
    }
}
