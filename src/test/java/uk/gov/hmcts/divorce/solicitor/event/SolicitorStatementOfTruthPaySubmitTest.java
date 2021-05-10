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
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TES_ORG_ID;

@ExtendWith(MockitoExtension.class)
public class SolicitorStatementOfTruthPaySubmitTest {

    private static final String STATEMENT_OF_TRUTH_ERROR_MESSAGE = "Statement of truth for solicitor and applicant 1 needs to be accepted";

    @Mock
    private SolicitorSubmitApplicationService solicitorSubmitApplicationService;

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

        when(solicitorSubmitApplicationService.getOrderSummary()).thenReturn(orderSummary);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(authorization);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorStatementOfTruthPaySubmit.aboutToStart(caseDetails);

        assertThat(response.getData().getSolApplicationFeeOrderSummary(), is(orderSummary));
        assertThat(response.getData().getPayments().size(), is(1));
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

        when(solicitorSubmitApplicationService.getOrderSummary()).thenReturn(orderSummary);
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

        assertThat(configBuilder.getEvents().get(0).getId(), is(SOLICITOR_STATEMENT_OF_TRUTH_PAY_SUBMIT));
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

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(SolicitorAwaitingPaymentConfirmation)
                .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(aboutToStartOrSubmitResponse);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

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
    void shouldSetApplicant2DigitalDetailsWhenRespSolicitorIsDigitalAndRespOrganisationIsSet() {
        final long caseId = 1L;
        final OrganisationPolicy<UserRole> organisationPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationId(TES_ORG_ID)
                .organisationName(TEST_ORG_NAME)
                .build()
            )
            .build();

        final CaseData caseData = CaseData.builder()
            .statementOfTruth(YES)
            .solSignStatementOfTruth(YES)
            .app2SolDigital(YES)
            .applicant2OrganisationPolicy(organisationPolicy)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(SolicitorAwaitingPaymentConfirmation)
                .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(aboutToStartOrSubmitResponse);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        final CaseData expectedCaseData = CaseData.builder()
            .statementOfTruth(YES)
            .solSignStatementOfTruth(YES)
            .app2SolDigital(YES)
            .app2ContactMethodIsDigital(YES)
            .applicant2SolicitorRepresented(YES)
            .applicant2OrganisationPolicy(organisationPolicy)
            .build();

        assertThat(response.getData(), is(expectedCaseData));
        assertThat(response.getState(), is(SolicitorAwaitingPaymentConfirmation));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldNotSetApplicant2DigitalDetailsWhenRespSolicitorIsNotDigital() {
        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .statementOfTruth(YES)
            .solSignStatementOfTruth(YES)
            .app2SolDigital(NO)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(SolicitorAwaitingPaymentConfirmation)
                .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(aboutToStartOrSubmitResponse);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SolicitorAwaitingPaymentConfirmation));
        assertThat(response.getErrors(), is(nullValue()));
    }

    @Test
    void shouldNotSetApplicant2DigitalDetailsWhenRespSolicitorIsDigitalAndRespOrgIsNotSet() {
        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .statementOfTruth(YES)
            .solSignStatementOfTruth(YES)
            .app2SolDigital(YES)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseDetails.setState(SOTAgreementPayAndSubmitRequired);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(SolicitorAwaitingPaymentConfirmation)
                .build();

        when(solicitorSubmitApplicationService.aboutToSubmit(caseData, caseId, APP_1_SOL_AUTH_TOKEN))
            .thenReturn(aboutToStartOrSubmitResponse);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorStatementOfTruthPaySubmit
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData(), is(caseData));
        assertThat(response.getState(), is(SolicitorAwaitingPaymentConfirmation));
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
            .solApplicationFeeOrderSummary(orderSummary)
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
            .solApplicationFeeOrderSummary(orderSummary)
            .payments(payments)
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorStatementOfTruthPaySubmit.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(SolicitorAwaitingPaymentConfirmation));
    }

    @Test
    void shouldSetStateToSolicitorAwaitingPaymentConfirmationWhenHelpWithFeesIsSelectedAndNoPaymentIsMade() {
        final long caseId = 1L;
        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder()
            .solApplicationFeeOrderSummary(orderSummary)
            .helpWithFeesAppliedForFees(YES)
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        solicitorStatementOfTruthPaySubmit.submitted(caseDetails, beforeCaseDetails);

        assertThat(caseDetails.getState(), is(SolicitorAwaitingPaymentConfirmation));
    }
}
