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
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.common.service.PaymentValidatorService;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_INCOMPLETE;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class CitizenPaymentMadeTest {

    private static final String STATEMENT_OF_TRUTH_ERROR_MESSAGE =
        "Statement of truth must be accepted by the person making the application";

    @Mock
    private ApplicationSubmittedNotification applicationSubmittedNotification;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private PaymentValidatorService paymentValidatorService;

    @InjectMocks
    private CitizenPaymentMade citizenPaymentMade;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenPaymentMade.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_PAYMENT_MADE);
    }

    @Test
    void givenPaymentWasInvalidThenSetStateToAwaitingPaymentAndDontSubmitApplication() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(Draft);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        List<ListValue<Payment>> payments = singletonList(new ListValue<>("1", Payment.builder().amount(55000).status(DECLINED).build()));
        caseData.getApplication().setApplicationPayments(payments);

        when(paymentValidatorService.validatePayments(payments, details.getId())).thenReturn(
            Collections.singletonList(ERROR_PAYMENT_INCOMPLETE)
        );

        final AboutToStartOrSubmitResponse<CaseData, State> result = citizenPaymentMade.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(caseData);
        assertThat(result.getState()).isEqualTo(AwaitingPayment);
        verifyNoInteractions(submissionService);
    }

    @Test
    void givenValidJointCaseDataWhenCallbackIsInvokedThenApplicationIsSubmittedAndSendEmailToApplicant1AndApplicant2() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
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

        when(paymentValidatorService.validatePayments(caseData.getApplication().getApplicationPayments(), details.getId())).thenReturn(
            Collections.emptyList()
        );

        when(submissionService.submitApplication(details)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> result = citizenPaymentMade.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(expectedCaseData);
        assertThat(result.getState()).isSameAs(Submitted);
        verify(submissionService).submitApplication(details);
    }

    @Test
    void shouldReturnErrorIfStatementOfTruthAndSolStatementOfTruthIsNotSet() {
        final var caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setApplicant1StatementOfTruth(null);
        caseData.getApplication().setSolSignStatementOfTruth(null);

        final var orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        final var payment = Payment.builder().amount(55000).status(SUCCESS).build();
        caseData.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(Draft);

        when(paymentValidatorService.validatePayments(caseData.getApplication().getApplicationPayments(), details.getId())).thenReturn(
            Collections.emptyList()
        );

        final AboutToStartOrSubmitResponse<CaseData, State> result = citizenPaymentMade.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(caseData);
        assertThat(result.getState()).isEqualTo(Draft);
        assertThat(result.getErrors()).contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE);
        verifyNoInteractions(submissionService);
    }

    @Test
    void shouldSetStateAsAwaitingDocumentsWhenSoleCaseAndApplicantDoesNotKnowRespondentAddressAndWishToServeByAlternativeMeans() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant1KnowsApplicant2Address(NO);
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);
        final CaseData expectedCaseData = CaseData.builder().build();

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().amount(55000).status(SUCCESS).build();
        caseData.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedCaseData);

        when(paymentValidatorService.validatePayments(caseData.getApplication().getApplicationPayments(), details.getId())).thenReturn(
            Collections.emptyList()
        );

        when(submissionService.submitApplication(details)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> result = citizenPaymentMade.aboutToSubmit(details, details);

        assertThat(result.getData()).isSameAs(expectedCaseData);
        assertThat(result.getState()).isSameAs(AwaitingDocuments);
        verify(submissionService).submitApplication(details);
    }
}
