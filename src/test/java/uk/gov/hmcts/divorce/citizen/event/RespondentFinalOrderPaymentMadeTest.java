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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.ApplyForFinalOrderService;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_INCOMPLETE;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class RespondentFinalOrderPaymentMadeTest {
    @Mock
    private ApplyForFinalOrderService applyForFinalOrderService;

    @Mock
    private PaymentValidatorService paymentValidatorService;

    @InjectMocks
    private RespondentFinalOrderPaymentMade respondentFinalOrderPaymentMade;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        respondentFinalOrderPaymentMade.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(RESPONDENT_FINAL_ORDER_PAYMENT_MADE);
    }

    @Test
    void givenPaymentWasInvalidThenSetStateToAwaitingFoPayment() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingFinalOrderPayment);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(55000).status(DECLINED).build()));
        caseData.getFinalOrder().setFinalOrderPayments(payments);
        when(paymentValidatorService.validatePayments(payments, details.getId())).thenReturn(
            Collections.singletonList(ERROR_PAYMENT_INCOMPLETE)
        );

        final AboutToStartOrSubmitResponse<CaseData, State> result = respondentFinalOrderPaymentMade.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(caseData);
        assertThat(result.getState()).isEqualTo(AwaitingFinalOrderPayment);
    }

    @Test
    void givenValidPaymentMadeWhenCallbackIsInvokedThenApplyForFinalOrderServiceIsInvoked() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(AwaitingFinalOrderPayment);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(55000).status(DECLINED).build()));
        caseData.getFinalOrder().setFinalOrderPayments(payments);

        when(paymentValidatorService.validatePayments(caseData.getFinalOrder().getFinalOrderPayments(), details.getId())).thenReturn(
            Collections.emptyList()
        );
        when(applyForFinalOrderService.applyForFinalOrderAsApplicant2(details)).thenReturn(details);

        final AboutToStartOrSubmitResponse<CaseData, State> result = respondentFinalOrderPaymentMade.aboutToSubmit(details, details);

        verify(applyForFinalOrderService).applyForFinalOrderAsApplicant2(details);
        assertThat(result.getState()).isEqualTo(details.getState());
    }

    @Test
    void shouldSendNotificationsByDelegatingToApplyForFinalOrderService() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(AwaitingFinalOrderPayment);

        respondentFinalOrderPaymentMade.submitted(details, details);

        verify(applyForFinalOrderService).sendRespondentAppliedForFinalOrderNotifications(details);
    }
}
