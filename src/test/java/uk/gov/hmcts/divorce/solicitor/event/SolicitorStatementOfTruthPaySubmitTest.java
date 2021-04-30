package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.MoneyGBP;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitPetitionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.common.model.State.SolicitorAwaitingPaymentConfirmation;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStatementOfTruthPaySubmit.SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class SolicitorStatementOfTruthPaySubmitTest {

    private static final String STATEMENT_OF_TRUTH_ERROR_MESSAGE = "Statement of truth for solicitor and applicant 1 needs to be accepted";

    @Mock
    private SolicitorSubmitPetitionService solicitorSubmitPetitionService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SolicitorStatementOfTruthPaySubmit solicitorStatementOfTruthPaySubmit;

    @Test
    void shouldSetOrderSummaryAndSolicitorRoles() {

        final long caseId = 1L;
        final String authorization = "authorization";
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(solicitorSubmitPetitionService.getOrderSummary()).thenReturn(orderSummary);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(authorization);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorStatementOfTruthPaySubmit.aboutToStart(caseDetails);

        assertThat(response.getData().getSolApplicationFeeOrderSummary(), is(orderSummary));
        assertThat(response.getData().getPayments().size(), is(1));
        verify(ccdAccessService).addPetitionerSolicitorRole(
            authorization,
            caseId
        );
    }

    @Test
    void shouldAddPaymentIfPaymentsExists() {

        final long caseId = 1L;
        final String authorization = "authorization";
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .paymentAmount(MoneyGBP.builder().amount(orderSummary.getPaymentTotal()).build())
            .paymentChannel("online")
            .paymentDate(LocalDate.now())
            .paymentFeeId("FEE0001")
            .paymentReference(orderSummary.getPaymentReference())
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.SUCCESS)
            .paymentTransactionId("Transaction1")
            .build();
        ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .id(UUID.randomUUID().toString())
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder()
            .payments(payments)
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(solicitorSubmitPetitionService.getOrderSummary()).thenReturn(orderSummary);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(authorization);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorStatementOfTruthPaySubmit.aboutToStart(caseDetails);

        assertThat(response.getData().getPayments().size(), is(2));
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        solicitorStatementOfTruthPaySubmit.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getEventID(), is(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnWithoutErrorIfStatementOfTruthAndSolStatementOfTruthAreSetToYes() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .statementOfTruth(YES)
            .solSignStatementOfTruth(YES)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);

        when(solicitorSubmitPetitionService.aboutToSubmit(caseData, caseId)).thenReturn(SolicitorAwaitingPaymentConfirmation);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SolicitorAwaitingPaymentConfirmation));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldReturnErrorIfStatementOfTruthIsSetToNo() {

        final CaseData caseData = CaseData.builder().build();
        caseData.setStatementOfTruth(NO);
        caseData.setSolSignStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SOTAgreementPayAndSubmitRequired));
        assertThat(response.getErrors(), contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnErrorIfStatementOfTruthIsNull() {

        final CaseData caseData = CaseData.builder().build();
        caseData.setSolSignStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SOTAgreementPayAndSubmitRequired));
        assertThat(response.getErrors(), contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnErrorIfSolStatementOfTruthIsSetToNo() {

        final CaseData caseData = CaseData.builder().build();
        caseData.setStatementOfTruth(YES);
        caseData.setSolSignStatementOfTruth(NO);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SOTAgreementPayAndSubmitRequired));
        assertThat(response.getErrors(), contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnErrorIfSolStatementOfTruthIsNull() {

        final CaseData caseData = CaseData.builder().build();
        caseData.setStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SOTAgreementPayAndSubmitRequired));
        assertThat(response.getErrors(), contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE));
    }

    @Test
    void shouldSetStateToSubmittedIfPaymentSuccessful() {
        final long caseId = 1L;
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .paymentAmount(MoneyGBP.builder().amount(orderSummary.getPaymentTotal()).build())
            .paymentChannel("online")
            .paymentDate(LocalDate.now())
            .paymentFeeId("FEE0001")
            .paymentReference(orderSummary.getPaymentReference())
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.SUCCESS)
            .paymentTransactionId("Transaction1")
            .build();
        ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .id(UUID.randomUUID().toString())
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder()
            .payments(payments)
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorStatementOfTruthPaySubmit.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(Submitted));
    }

    @Test
    void shouldSetStateToSolicitorAwaitingPaymentConfirmationIfPaymentNotYetSuccessful() {
        final long caseId = 1L;
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .paymentAmount(MoneyGBP.builder().amount(orderSummary.getPaymentTotal()).build())
            .paymentChannel("online")
            .paymentDate(LocalDate.now())
            .paymentFeeId("FEE0001")
            .paymentReference(orderSummary.getPaymentReference())
            .paymentSiteId("AA04")
            .paymentStatus(PaymentStatus.TIMED_OUT)
            .paymentTransactionId("Transaction1")
            .build();
        ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .id(UUID.randomUUID().toString())
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder()
            .payments(payments)
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorStatementOfTruthPaySubmit.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(SolicitorAwaitingPaymentConfirmation));
    }
}
