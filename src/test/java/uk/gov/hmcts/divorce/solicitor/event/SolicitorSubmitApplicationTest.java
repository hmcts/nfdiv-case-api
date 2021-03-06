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
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorSubmitApplicationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitApplication.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitApplicationTest {

    private static final String STATEMENT_OF_TRUTH_ERROR_MESSAGE =
        "Statement of truth must be accepted by the person making the application";

    @Mock
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SolicitorSubmitApplication solicitorSubmitApplication;

    @Test
    void shouldSetOrderSummaryAndSolicitorRoles() {

        final long caseId = 1L;
        final String authorization = "authorization";
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentService.getOrderSummary()).thenReturn(orderSummary);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(authorization);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToStart(caseDetails);

        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary(), is(orderSummary));
        verify(ccdAccessService).addApplicant1SolicitorRole(
            authorization,
            caseId
        );
    }

    @Test
    void shouldAddPaymentIfPaymentsExists() {

        final long caseId = 1L;
        final String authorization = "authorization";
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .paymentAmount(parseInt(orderSummary.getPaymentTotal()))
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

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData().getPayments().size(), is(2));
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        solicitorSubmitApplication.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(SOLICITOR_SUBMIT));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnWithoutErrorIfStatementOfTruthOrSolStatementOfTruthAreSetToYes() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(Draft);

        final CaseInfo caseInfo = CaseInfo.builder()
            .caseData(caseData)
            .state(AwaitingPayment)
            .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(caseInfo);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(AwaitingPayment));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldReturnErrorIfStatementOfTruthAndSolStatementOfTruthIsSetToNo() {

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setStatementOfTruth(NO);
        caseData.getApplication().setSolSignStatementOfTruth(NO);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(Draft));
        assertThat(response.getErrors(), contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnWithoutErrorIfStatementOfTruthIsNull() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(caseId);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final CaseInfo caseInfo = CaseInfo.builder()
            .caseData(caseData)
            .state(Submitted)
            .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(caseInfo);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(Submitted));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldReturnWithoutErrorIfSolStatementOfTruthIsNull() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(caseId);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final CaseInfo caseInfo = CaseInfo.builder()
            .caseData(caseData)
            .state(Submitted)
            .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(caseInfo);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(Submitted));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldSetApplicant2DigitalDetailsWhenApp2SolicitorIsDigitalAndApp2OrganisationIsSet() {
        final long caseId = 1L;
        final OrganisationPolicy<UserRole> organisationPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationId(TEST_ORG_ID)
                .organisationName(TEST_ORG_NAME)
                .build()
            )
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setStatementOfTruth(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        caseData.getApplicant2().setSolicitor(Solicitor.builder().isDigital(YES).organisationPolicy(organisationPolicy).build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(Draft);

        final CaseInfo caseInfo = CaseInfo.builder()
            .caseData(caseData)
            .state(AwaitingPayment)
            .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(caseInfo);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        final CaseData expectedCaseData = CaseData.builder()
            .payments(singletonList(new ListValue<Payment>(null, null)))
            .build();

        expectedCaseData.getApplication().setStatementOfTruth(YES);
        expectedCaseData.getApplication().setSolSignStatementOfTruth(YES);
        expectedCaseData.getApplication().setApp2ContactMethodIsDigital(YES);
        expectedCaseData.getApplicant2().setSolicitor(Solicitor.builder().isDigital(YES).organisationPolicy(organisationPolicy).build());
        expectedCaseData.getApplicant2().setSolicitorRepresented(YES);

        assertThat(response.getData(), is(expectedCaseData));
        assertThat(response.getState(), is(AwaitingPayment));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldNotSetApplicant2DigitalDetailsWhenApp2SolicitorIsNotDigital() {
        final long caseId = 1L;
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setStatementOfTruth(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder().isDigital(NO).build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(Draft);

        final CaseInfo caseInfo = CaseInfo.builder()
            .caseData(caseData)
            .state(AwaitingPayment)
            .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(caseInfo);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(AwaitingPayment));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldNotSetApplicant2DigitalDetailsWhenApp2SolicitorIsDigitalAndApp2OrgIsNotSet() {
        final long caseId = 1L;
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setStatementOfTruth(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder().isDigital(YES).build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(Draft);

        final CaseInfo caseInfo = CaseInfo.builder()
            .caseData(caseData)
            .state(AwaitingPayment)
            .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(caseInfo);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(AwaitingPayment));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldSetStateToSubmittedIfPaymentSuccessful() {
        final long caseId = 1L;
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .paymentAmount(parseInt(orderSummary.getPaymentTotal()))
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
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder()
            .payments(payments)
            .build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorSubmitApplication.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(Submitted));
    }

    @Test
    void shouldSetStateToAwaitingPaymentIfPaymentNotYetSuccessful() {
        final long caseId = 1L;
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .paymentAmount(parseInt(orderSummary.getPaymentTotal()))
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
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder()
            .payments(payments)
            .build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorSubmitApplication.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(AwaitingPayment));
    }

    @Test
    void shouldSetStateToAwaitingPaymentWhenHelpWithFeesIsSelectedAndNoPaymentIsMade() {
        final long caseId = 1L;
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setHelpWithFees(
            HelpWithFees.builder()
                .appliedForFees(YES)
                .build()
        );
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorSubmitApplication.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(AwaitingPayment));
    }
}
