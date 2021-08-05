package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.model.Payment;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAddPayment.CITIZEN_ADD_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.CANCELLED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class CitizenAddPaymentTest {

    @Mock
    private SubmissionService submissionService;

    @InjectMocks
    private CitizenAddPayment citizenAddPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenAddPayment.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_ADD_PAYMENT);
    }

    @Test
    public void givenLastPaymentInProgressCaseDataWhenCallbackIsInvokedThenSetToAwaitingPayment() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setApplicant1StatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().amount(55000).status(IN_PROGRESS).build();
        caseData.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = citizenAddPayment.aboutToSubmit(details, details);

        verifyNoInteractions(submissionService);
        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    public void givenUnsuccessfulPaymentCaseDataWhenCallbackIsInvokedThenSetToDraft() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setApplicant1StatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().amount(55000).status(CANCELLED).build();
        caseData.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = citizenAddPayment.aboutToSubmit(details, details);

        verifyNoInteractions(submissionService);
        assertThat(response.getState()).isEqualTo(Draft);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenApplicationIsSubmitted() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setApplicant1StatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);
        final CaseData expectedCaseData = CaseData.builder().build();

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().amount(55000).status(SUCCESS).build();
        caseData.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedCaseData);
        expectedDetails.setState(Submitted);

        when(submissionService.submitApplication(details)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> result = citizenAddPayment.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(expectedCaseData);
        assertThat(result.getState()).isSameAs(Submitted);
        verify(submissionService).submitApplication(details);
    }

    @Test
    public void givenInvalidPaymentWhenThenDontSubmitApplication() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().amount(55000).status(DECLINED).build();
        caseData.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(Draft);

        final AboutToStartOrSubmitResponse<CaseData, State> result = citizenAddPayment.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(caseData);
        assertThat(result.getState()).isEqualTo(Draft);
        verifyNoInteractions(submissionService);
    }
}
