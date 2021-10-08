package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Fee;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.CASEWORKER_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.EVENT_MISC;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.KEYWORD_DEEMED;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAlternativeServicePayment.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CaseworkerConfirmServicePaymentTest {

    @Mock
    PaymentService paymentService;

    @InjectMocks
    private CaseworkerAlternativeServicePayment alternativeServicePayment;

    final OrderSummary orderSummaryBailiff = mock(OrderSummary.class);
    final OrderSummary orderSummaryDeemedDispensed = mock(OrderSummary.class);

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        alternativeServicePayment.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SERVICE_PAYMENT);
    }

    @Test
    void shouldSetBailiffFeeWhenAlternativeServiceTypeIsBailiff() {

        final Fee fee = Fee.builder()
            .code("FEE0392")
            .amount("4500")
            .build();

        final ListValue<Fee> listValue = ListValue.<Fee>builder()
            .id(UUID.randomUUID().toString())
            .value(fee)
            .build();

        final List<ListValue<Fee>> orderSummaryFees = new ArrayList<>();
        orderSummaryFees.add(listValue);

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_MISC, KEYWORD_BAILIFF)).thenReturn(orderSummaryBailiff);
        when(orderSummaryBailiff.getFees()).thenReturn(orderSummaryFees);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingServicePayment);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            alternativeServicePayment.aboutToStart(details);

        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServicePaymentFeeOrderSummary()).
            isEqualTo(orderSummaryBailiff);

        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServicePaymentFeeOrderSummary().getFees()).
            isEqualTo(orderSummaryFees);

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

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_DEEMED)).thenReturn(orderSummaryDeemedDispensed);
        when(orderSummaryDeemedDispensed.getFees()).thenReturn(orderSummaryFees);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingServicePayment);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            alternativeServicePayment.aboutToStart(details);

        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServicePaymentFeeOrderSummary()).
            isEqualTo(orderSummaryDeemedDispensed);

        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServicePaymentFeeOrderSummary().getFees()).
            isEqualTo(orderSummaryFees);
    }
}
