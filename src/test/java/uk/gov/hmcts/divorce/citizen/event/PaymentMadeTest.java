package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.payment.model.Payment;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.PaymentMade.PAYMENT_MADE;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
public class PaymentMadeTest {
    @Mock
    private ApplicationSubmittedNotification notification;

    @Mock
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @InjectMocks
    private PaymentMade paymentMade;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        paymentMade.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(PAYMENT_MADE));
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() {
        final CaseData caseData = caseData();
        caseData.setApplicant1Email(TEST_USER_EMAIL);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        paymentMade.aboutToSubmit(details, details);

        verify(notification).send(caseData, details.getId());
    }

    @Test
    public void givenInvalidPaymentWhenThenDontSendEmail() {
        final CaseData caseData = caseData();
        caseData.setApplicant1Email(TEST_USER_EMAIL);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(DECLINED).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        paymentMade.aboutToSubmit(details, details);

        verifyNoInteractions(notification);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendOutstandingActionEmail() {
        final CaseData caseData = caseData();
        caseData.setApplicant1Email(TEST_USER_EMAIL);
        caseData.setApplicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse response = paymentMade.aboutToSubmit(details, details);

        verify(outstandingActionNotification).send(caseData, details.getId());
        verify(notification).send(caseData, details.getId());
        assertThat(response.getState(), is(AwaitingDocuments));
    }

    @Test
    public void givenCallbackIsInvokedThenSendOutstandingActionEmailForCannotUploadSupportingDocument() {
        final CaseData caseData = caseData();
        caseData.setApplicant1Email(TEST_USER_EMAIL);

        Set<DocumentType> docs = new HashSet<>();
        docs.add(DocumentType.MARRIAGE_CERTIFICATE);
        docs.add(DocumentType.NAME_CHANGE_EVIDENCE);
        caseData.setCannotUploadSupportingDocument(docs);

        Payment payment = Payment.builder().paymentAmount(55000).paymentStatus(SUCCESS).build();
        caseData.setPayments(singletonList(new ListValue<>("1", payment)));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse response = paymentMade.aboutToSubmit(details, details);

        verify(outstandingActionNotification).send(caseData, details.getId());
        verify(notification).send(caseData, details.getId());
        assertThat(response.getState(), is(AwaitingDocuments));
    }
}
