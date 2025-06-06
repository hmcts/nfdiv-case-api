package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.ApplyForFinalOrderService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class RespondentApplyForFinalOrderTest {
    @Mock
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Mock
    private PaymentSetupService paymentSetupService;

    @InjectMocks
    private RespondentApplyForFinalOrder respondentApplyForFinalOrder;

    private OrderSummary orderSummary;

    @Test
    void givenRespondentDoesNotNeedHwfThenChangeStateToAwaitingPaymentAndSetOrderSummary() {
        CaseData caseData = CaseData.builder()
            .finalOrder(
                FinalOrder.builder()
                    .applicant2FinalOrderHelpWithFees(HelpWithFees.builder().needHelp(YesOrNo.NO).build())
                    .build()
            )
            .applicant2(Applicant.builder().firstName("test").build())
            .build();

        final long caseId = TEST_CASE_ID;
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();
        final var orderSummary = orderSummary();
        caseDetails.setId(caseId);

        when(paymentSetupService.createFinalOrderFeeOrderSummary(caseData, TEST_CASE_ID))
            .thenReturn(orderSummary);

        when(paymentSetupService.createFinalOrderFeeServiceRequest(
            caseData, caseId, orderSummary
        )).thenReturn(TEST_SERVICE_REFERENCE);


        final AboutToStartOrSubmitResponse<CaseData, State> response = respondentApplyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);
        FinalOrder finalOrder = response.getData().getFinalOrder();

        assertThat(response.getState()).isEqualTo(State.AwaitingFinalOrderPayment);
        assertThat(finalOrder.getApplicant2FinalOrderFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(finalOrder.getApplicant2FinalOrderFeeInPounds()).isEqualTo("550");
        assertThat(finalOrder.getApplicant2FinalOrderFeeServiceRequestReference()).isEqualTo(TEST_SERVICE_REFERENCE);
    }

    @Test
    void givenRespondentDoesNeedHwfThenDelegateToApplyForFinalOrderService() {
        CaseData caseData = CaseData.builder().finalOrder(
            FinalOrder.builder()
                .applicant2FinalOrderHelpWithFees(HelpWithFees.builder().needHelp(YesOrNo.YES).build())
                .build()
        ).build();
        final var caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        when(applyForFinalOrderService.applyForFinalOrderAsApplicant2(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = respondentApplyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        verify(applyForFinalOrderService).applyForFinalOrderAsApplicant2(caseDetails);
        assertThat(response.getData().getFinalOrder().getApplicant2FinalOrderFeeOrderSummary()).isNull();
        assertThat(response.getState()).isEqualTo(caseDetails.getState());
    }

    private OrderSummary orderSummary() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .build();
    }
}
