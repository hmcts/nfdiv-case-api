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
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationSubmitPaymentServiceTest {

    @InjectMocks
    private GeneralApplicationSubmitPaymentService generalApplicationSubmitPaymentService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Clock clock;

    @Test
    void shouldRecordAlternatePaymentAndReturnNoErrorWhenPaymentMethodIsNotPba() {
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder()
                .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF)
                .build())
            .build();
        CaseData caseData = CaseData.builder().generalApplication(generalApplication).build();
        Applicant applicant = Applicant.builder().build();

        Optional<String> result = generalApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, applicant);

        assertThat(result).isEmpty();
        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldReturnErrorWhenPbaNumberMissing() {
        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder()
                .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT)
                .pbaNumbers(null)
                .serviceRequestReference("SR123")
                .build())
            .build();
        CaseData caseData = CaseData.builder().generalApplication(generalApplication).build();
        Applicant applicant = Applicant.builder().build();

        Optional<String> result = generalApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, applicant);

        assertThat(result).contains("PBA number not present when payment method is 'Solicitor fee account (PBA)'");
        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldReturnErrorWhenServiceRequestReferenceMissing() {
        DynamicList pbaNumbers = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0001").build())
            .build();

        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(FeeDetails.builder()
                .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT)
                .pbaNumbers(pbaNumbers)
                .serviceRequestReference(null)
                .build())
            .build();
        CaseData caseData = CaseData.builder().generalApplication(generalApplication).build();
        Applicant applicant = Applicant.builder().build();

        Optional<String> result = generalApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, applicant);

        assertThat(result).contains("Service request reference is missing for PBA payment");
        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldUpdateCaseDataAndRecordPaymentWhenPbaPaymentSucceeds() {
        DynamicList pbaNumbers = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0001").build())
            .build();

        FeeDetails feeDetails = FeeDetails.builder()
            .paymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT)
            .pbaNumbers(pbaNumbers)
            .orderSummary(OrderSummary.builder().paymentTotal("100").fees(List.of(ListValue.<Fee>builder()
                .id("1")
                .value(Fee.builder()
                    .code("FEE0002")
                    .build())
                .build())).build())
            .serviceRequestReference("SR123")
            .build();

        GeneralApplication generalApplication = GeneralApplication.builder()
            .generalApplicationFee(feeDetails)
            .build();

        CaseData caseData = CaseData.builder().generalApplication(generalApplication).build();
        Applicant applicant = Applicant.builder().build();

        PbaResponse pbaResponse = new PbaResponse(CREATED, null, "1234");
        when(paymentService.processPbaPayment(eq(TEST_CASE_ID), eq("SR123"), eq(applicant.getSolicitor()), eq("PBA0001"),
            eq(feeDetails.getOrderSummary()), eq(feeDetails.getAccountReferenceNumber()))).thenReturn(pbaResponse);
        when(clock.instant()).thenReturn(Instant.parse("2026-10-07T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        Optional<String> result = generalApplicationSubmitPaymentService.processSubmitPayment(TEST_CASE_ID, caseData, applicant);

        assertThat(result).isEmpty();
    }
}
