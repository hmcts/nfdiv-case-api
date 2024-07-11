package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEEMED;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AlternativeServicePaymentServiceTest {

    @Mock
    PaymentService paymentService;

    @InjectMocks
    private AlternativeServicePaymentService alternativeServicePaymentService;

    final OrderSummary orderSummaryBailiff = mock(OrderSummary.class);
    final OrderSummary orderSummaryDeemedDispensed = mock(OrderSummary.class);

    @Test
    void shouldSetBailiffFeeWhenAlternativeServiceTypeIsBailiff() {

        final Fee fee = Fee.builder()
            .code("FEE0391")
            .amount("4500")
            .build();

        final ListValue<Fee> listValue = ListValue.<Fee>builder()
            .id(UUID.randomUUID().toString())
            .value(fee)
            .build();

        final List<ListValue<Fee>> orderSummaryFees = new ArrayList<>();
        orderSummaryFees.add(listValue);

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ENFORCEMENT, KEYWORD_BAILIFF))
            .thenReturn(orderSummaryBailiff);
        when(orderSummaryBailiff.getFees()).thenReturn(orderSummaryFees);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);

        CaseData response = alternativeServicePaymentService.getFeeAndSetOrderSummary(caseData, TEST_CASE_ID);

        assertThat(response.getAlternativeService().getServicePaymentFee().getOrderSummary())
            .isEqualTo(orderSummaryBailiff);

        assertThat(response.getAlternativeService().getServicePaymentFee().getOrderSummary().getFees())
            .isEqualTo(orderSummaryFees);
    }

    @Test
    void shouldSetDispensedFeeWhenAlternativeServiceTypeIsNotBailiff() {

        final Fee fee = Fee.builder()
            .code("FEE0228")
            .amount("5300")
            .build();

        final ListValue<Fee> listValue = ListValue.<Fee>builder()
            .id(UUID.randomUUID().toString())
            .value(fee)
            .build();

        final List<ListValue<Fee>> orderSummaryFees = new ArrayList<>();
        orderSummaryFees.add(listValue);

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_DEEMED))
            .thenReturn(orderSummaryDeemedDispensed);
        when(orderSummaryDeemedDispensed.getFees()).thenReturn(orderSummaryFees);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);

        CaseData response = alternativeServicePaymentService.getFeeAndSetOrderSummary(caseData, TEST_CASE_ID);

        assertThat(response.getAlternativeService().getServicePaymentFee().getOrderSummary())
            .isEqualTo(orderSummaryDeemedDispensed);

        assertThat(response.getAlternativeService().getServicePaymentFee().getOrderSummary().getFees())
            .isEqualTo(orderSummaryFees);
    }
}
