package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentSetupService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateServiceRequest.CITIZEN_CREATE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_PAYMENT_CALLBACK_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenCreateServiceRequestTest {
    @Mock
    private PaymentSetupService paymentSetupService;

    @InjectMocks
    private CitizenCreateServiceRequest citizenCreateServiceRequest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenCreateServiceRequest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_CREATE_SERVICE_REQUEST);
    }

    @Test
    void shouldSetServiceRequestForApplicationPaymentIfCaseIsInAwaitingPayment() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = new OrderSummary();
        caseData.setCitizenPaymentCallbackUrl(TEST_PAYMENT_CALLBACK_URL);
        long caseId = TEST_CASE_ID;

        caseDetails.setState(AwaitingPayment);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentSetupService.createApplicationFeeOrderSummary(caseData, caseId))
            .thenReturn(orderSummary);
        when(paymentSetupService.createApplicationFeeServiceRequest(caseData, caseId, TEST_PAYMENT_CALLBACK_URL))
            .thenReturn(TEST_SERVICE_REFERENCE);

        var response = citizenCreateServiceRequest.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplication().getApplicationFeeServiceRequestReference())
            .isEqualTo(TEST_SERVICE_REFERENCE);
        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary())
            .isEqualTo(orderSummary);
    }

    @Test
    void shouldSetServiceRequestForFinalOrderPaymentIfCaseIsInAwaitingFinalOrderPayment() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = new CaseData();
        final OrderSummary orderSummary = OrderSummary.builder().build();

        caseDetails.setState(AwaitingFinalOrderPayment);
        caseData.getFinalOrder().setApplicant2FinalOrderFeeOrderSummary(orderSummary);
        caseData.setCitizenPaymentCallbackUrl(TEST_PAYMENT_CALLBACK_URL);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(paymentSetupService.createFinalOrderFeeOrderSummary(caseData, TEST_CASE_ID))
                .thenReturn(orderSummary);
        when(paymentSetupService.createFinalOrderFeeServiceRequest(caseData, TEST_CASE_ID, TEST_PAYMENT_CALLBACK_URL, orderSummary))
            .thenReturn(TEST_SERVICE_REFERENCE);

        var response = citizenCreateServiceRequest.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getFinalOrder().getApplicant2FinalOrderFeeServiceRequestReference())
            .isEqualTo(TEST_SERVICE_REFERENCE);
        assertThat(response.getData().getFinalOrder().getApplicant2FinalOrderFeeOrderSummary())
            .isEqualTo(orderSummary);
        assertThat(response.getData().getFinalOrder().getApplicant2SolFinalOrderFeeOrderSummary())
            .isEqualTo(orderSummary);
    }
}
