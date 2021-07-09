package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAddPayment.CITIZEN_ADD_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.CANCELLED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
public class CitizenAddPaymentTest {
    @Mock
    private ApplicationSubmittedNotification notification;

    @Mock
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @InjectMocks
    private CitizenAddPayment citizenAddPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenAddPayment.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(CITIZEN_ADD_PAYMENT));
    }

    @Test
    public void givenLastPaymentInProgressCaseDataWhenCallbackIsInvokedThenSetToAwaitingPayment() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(IN_PROGRESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = citizenAddPayment.aboutToSubmit(details, details);

        verifyNoInteractions(notification);
        assertThat(response.getState(), is(AwaitingPayment));
    }

    @Test
    public void givenUnsuccessfulPaymentCaseDataWhenCallbackIsInvokedThenSetToDraft() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(CANCELLED).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = citizenAddPayment.aboutToSubmit(details, details);

        verifyNoInteractions(notification);
        assertThat(response.getState(), is(Draft));
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setStatementOfTruth(YesOrNo.YES);
        caseData.getApplication().setSolSignStatementOfTruth(YesOrNo.YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        citizenAddPayment.aboutToSubmit(details, details);

        verify(notification).send(caseData, details.getId());
    }

    @Test
    public void givenInvalidPaymentWhenThenDontSendEmail() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(DECLINED).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        citizenAddPayment.aboutToSubmit(details, details);

        verifyNoInteractions(notification);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendOutstandingActionEmail() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = citizenAddPayment.aboutToSubmit(details, details);

        verify(outstandingActionNotification).send(caseData, details.getId());
        verify(notification).send(caseData, details.getId());
        assertThat(response.getState(), is(AwaitingDocuments));
    }

    @Test
    public void givenCallbackIsInvokedThenSendOutstandingActionEmailForCannotUploadSupportingDocument() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        caseData.getApplication().setCannotUploadSupportingDocument(docs);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final var response = citizenAddPayment.aboutToSubmit(details, details);

        verify(outstandingActionNotification).send(caseData, details.getId());
        verify(notification).send(caseData, details.getId());
        assertThat(response.getState(), is(AwaitingDocuments));
    }
}
