package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateServiceRequest.CITIZEN_CREATE_SERVICE_REQUEST;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class CitizenCreateServiceRequestTest {
    @Mock
    private PaymentService paymentService;

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
    public void shouldSetServiceRequestForApplicationPaymentIfCaseIsInAwaitingPayment() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        final long caseId = TEST_CASE_ID;

        caseDetails.setState(AwaitingPayment);
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentService.createServiceRequestReference(
            null, caseId, caseData.getApplicant1().getFullName(), orderSummary
        )).thenReturn(TEST_SERVICE_REFERENCE);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenCreateServiceRequest.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplication().getApplicationFeeServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    public void shouldSetServiceRequestForFinalOrderPaymentIfCaseIsInAwaitingFoPayment() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().divorceOrDissolution(DIVORCE).build();
        final OrderSummary orderSummary = OrderSummary.builder().build();
        final long caseId = TEST_CASE_ID;

        caseDetails.setState(AwaitingFinalOrderPayment);
        caseData.getFinalOrder().setApplicant2FinalOrderFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentService.createServiceRequestReference(
            null, caseId, caseData.getApplicant1().getFullName(), orderSummary
        )).thenReturn(TEST_SERVICE_REFERENCE);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenCreateServiceRequest.aboutToSubmit(caseDetails, caseDetails);

        assertThat(
            response.getData().getFinalOrder().getApplicant2FinalOrderFeeServiceRequestReference()
        ).isEqualTo(TEST_SERVICE_REFERENCE);
    }
}
