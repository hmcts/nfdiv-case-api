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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class RespondentApplyForFinalOrderTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private RespondentApplyForFinalOrder respondentApplyForFinalOrder;

    private OrderSummary orderSummary;

    @Test
    public void givenRespondentDoesNotNeedHwfThenChangeStateAndSetOrderSummary() {
        final long caseId = TEST_CASE_ID;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().finalOrder(
            FinalOrder.builder()
                .applicant2FinalOrderHelpWithFees(HelpWithFees.builder().needHelp(YesOrNo.NO).build())
                .build()
        ).build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        var orderSummary = orderSummary();

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE))
            .thenReturn(orderSummary());

        final AboutToStartOrSubmitResponse<CaseData, State> response = respondentApplyForFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingFinalOrderPayment);
        assertThat(response.getData().getFinalOrder().getApplicant2FinalOrderFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getFinalOrder().getApplicant2FinalOrderFeeInPounds()).isEqualTo("550");
    }

    private OrderSummary orderSummary() {
        return OrderSummary
            .builder()
            .paymentTotal("55000")
            .build();
    }
}
