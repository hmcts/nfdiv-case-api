package uk.gov.hmcts.divorce.solicitor.event;

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
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.hmcts.divorce.payment.model.PaymentStatus;
import uk.gov.hmcts.divorce.payment.model.PbaResponse;
import uk.gov.hmcts.divorce.solicitor.event.page.SolPayment;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.lang.Integer.parseInt;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitApplication.SOLICITOR_SUBMIT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getFeeListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getPbaNumbersForAccount;

@ExtendWith(MockitoExtension.class)
public class SolicitorSubmitApplicationTest {

    private static final String STATEMENT_OF_TRUTH_ERROR_MESSAGE =
        "Statement of truth must be accepted by the person making the application";

    @Mock
    private PaymentService paymentService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private SolPayment solPayment;

    @InjectMocks
    private SolicitorSubmitApplication solicitorSubmitApplication;

    @Test
    void shouldSetOrderSummaryAndSolicitorFeesInPoundsAndSolicitorRolesAndPbaNumbersWhenAboutToStartIsInvoked() {

        final long caseId = 1L;
        final String authorization = "authorization";
        final OrderSummary orderSummary = mock(OrderSummary.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        when(paymentService.getOrderSummary()).thenReturn(orderSummary);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(authorization);
        when(orderSummary.getPaymentTotal()).thenReturn("55000");

        var midEventCaseData = caseData();
        midEventCaseData.getApplication().setPbaNumbers(getPbaNumbersForAccount("PBA0012345"));

        var pbaResponse
            = AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(midEventCaseData)
            .build();

        when(solPayment.midEvent(caseDetails, caseDetails)).thenReturn(pbaResponse);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToStart(caseDetails);

        assertThat(response.getData().getApplication().getApplicationFeeOrderSummary()).isEqualTo(orderSummary);
        assertThat(response.getData().getApplication().getSolApplicationFeeInPounds()).isEqualTo("550");

        verify(ccdAccessService).addApplicant1SolicitorRole(
            authorization,
            caseId
        );
    }

    @Test
    void shouldAddPaymentIfPaymentsExists() {

        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .created(LocalDateTime.now())
            .feeCode("FEE0001")
            .reference(orderSummary.getPaymentReference())
            .status(PaymentStatus.SUCCESS)
            .transactionId("Transaction1")
            .build();
        final ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .id(UUID.randomUUID().toString())
            .value(payment)
            .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .applicationPayments(payments)
                .solPaymentHowToPay(FEE_PAY_BY_ACCOUNT)
                .solSignStatementOfTruth(YES)
                .applicationFeeOrderSummary(OrderSummary.builder()
                    .paymentTotal("55000")
                    .fees(singletonList(getFeeListValue()))
                    .build())
                .build())
            .build();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        PbaResponse pbaResponse = new PbaResponse(CREATED, null, "1234");
        when(paymentService.processPbaPayment(caseData, TEST_CASE_ID, null)).thenReturn(pbaResponse);

        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();
        expectedCaseDetails.setId(TEST_CASE_ID);
        expectedCaseDetails.setData(caseData);
        expectedCaseDetails.setState(Submitted);

        when(submissionService.submitApplication(caseDetails)).thenReturn(expectedCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData().getApplication().getApplicationPayments()).hasSize(2);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorSubmitApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_SUBMIT);
    }

    @Test
    void shouldReturnWithoutErrorIfStatementOfTruthOrSolStatementOfTruthAreSetToYes() {

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setApplicationFeeOrderSummary(OrderSummary.builder()
            .paymentTotal("55000")
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();
        expectedCaseDetails.setId(TEST_CASE_ID);
        expectedCaseDetails.setData(caseData);
        expectedCaseDetails.setState(AwaitingPayment);

        when(submissionService.submitApplication(caseDetails)).thenReturn(expectedCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(AwaitingPayment);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorIfStatementOfTruthAndSolStatementOfTruthIsSetToNo() {

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setApplicant1StatementOfTruth(NO);
        caseData.getApplication().setSolSignStatementOfTruth(NO);
        caseData.getApplication().setApplicationFeeOrderSummary(OrderSummary.builder()
            .paymentTotal("55000")
            .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(Draft);
        assertThat(response.getErrors()).contains(STATEMENT_OF_TRUTH_ERROR_MESSAGE);
    }

    @Test
    void shouldReturnWithoutErrorIfStatementOfTruthIsNull() {

        final var caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setApplicationFeeOrderSummary(OrderSummary.builder()
            .paymentTotal("55000")
            .build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Draft);
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        mockExpectedCaseDetails(caseDetails, caseData, Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(Submitted);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnWithoutErrorIfSolStatementOfTruthIsNull() {

        final var caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setApplicationFeeOrderSummary(
            OrderSummary
                .builder()
                .paymentTotal("55000")
                .fees(singletonList(getFeeListValue()))
                .build()
        );

        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        mockExpectedCaseDetails(caseDetails, caseData, Draft);

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> expectedCaseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .state(Submitted)
            .build();

        when(submissionService.submitApplication(caseDetails)).thenReturn(expectedCaseDetails);

        var pbaResponse = new PbaResponse(CREATED, null, "1234");
        when(paymentService.processPbaPayment(caseData, TEST_CASE_ID, null)).thenReturn(pbaResponse);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
        assertThat(response.getState()).isEqualTo(Submitted);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSetApplicant2DigitalDetailsWhenApp2HasSolicitorAndApp2OrganisationIsSet() {
        final OrganisationPolicy<UserRole> organisationPolicy = OrganisationPolicy.<UserRole>builder()
            .organisation(Organisation
                .builder()
                .organisationId(TEST_ORG_ID)
                .organisationName(TEST_ORG_NAME)
                .build()
            )
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setApplicationFeeOrderSummary(OrderSummary.builder()
            .paymentTotal("55000")
            .build());

        caseData.getApplicant2().setSolicitor(Solicitor.builder().organisationPolicy(organisationPolicy).build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        mockExpectedCaseDetails(caseDetails, caseData, Draft);

        final AboutToStartOrSubmitResponse<CaseData, State> response = solicitorSubmitApplication
            .aboutToSubmit(caseDetails, new CaseDetails<>());

        assertThat(response.getData().getApplication().getApp2ContactMethodIsDigital()).isEqualTo(YES);
    }

    @Test
    void shouldSetStateToSubmittedIfPaymentSuccessful() {

        final OrderSummary orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final Payment payment = Payment
            .builder()
            .created(LocalDateTime.now())
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode("FEE0001")
            .reference(orderSummary.getPaymentReference())
            .status(PaymentStatus.SUCCESS)
            .transactionId("Transaction1")
            .build();
        ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseData.getApplication().setApplicationPayments(payments);
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        mockExpectedCaseDetails(caseDetails, caseData, Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
    }

    @Test
    void shouldSetStateToAwaitingPaymentIfPaymentNotYetSuccessful() {

        final var orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final var payment = Payment
            .builder()
            .created(LocalDateTime.now())
            .amount(parseInt(orderSummary.getPaymentTotal()))
            .channel("online")
            .feeCode("FEE0001")
            .reference(orderSummary.getPaymentReference())
            .status(PaymentStatus.TIMED_OUT)
            .transactionId("Transaction1")
            .build();
        ListValue<Payment> paymentListValue = ListValue
            .<Payment>builder()
            .value(payment)
            .build();
        List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);
        final var caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseData.getApplication().setApplicationPayments(payments);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        mockExpectedCaseDetails(caseDetails, caseData, AwaitingPayment);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetStateToAwaitingPaymentWhenHelpWithFeesIsSelectedAndNoPaymentIsMade() {

        final var orderSummary = OrderSummary.builder().paymentTotal("1000").build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final var caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setApplicant1HelpWithFees(
            HelpWithFees.builder()
                .appliedForFees(YES)
                .build()
        );
        caseData.getApplication().setApplicationFeeOrderSummary(orderSummary);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        mockExpectedCaseDetails(caseDetails, caseData, AwaitingPayment);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldSetStateToSubmittedIfPaymentMethodIsPbaAndPbaPaymentIsProcessedSuccessfully() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        caseData.getApplication().setApplicationFeeOrderSummary(
            OrderSummary
                .builder()
                .paymentTotal("55000")
                .fees(singletonList(getFeeListValue()))
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var pbaResponse = new PbaResponse(CREATED, null, "1234");
        when(paymentService.processPbaPayment(caseData, TEST_CASE_ID, null)).thenReturn(pbaResponse);

        mockExpectedCaseDetails(caseDetails, caseData, Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
    }

    @Test
    void shouldReturnErrorMessageIfPaymentMethodIsPbaAndPbaPaymentIsIsNotSuccessful() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();

        final var caseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        caseData.getApplication().setApplicationFeeOrderSummary(
            OrderSummary
                .builder()
                .paymentTotal("55000")
                .fees(singletonList(getFeeListValue()))
                .build()
        );

        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var pbaResponse = new PbaResponse(FORBIDDEN, "Account balance insufficient", null);
        when(paymentService.processPbaPayment(caseData, TEST_CASE_ID, null)).thenReturn(pbaResponse);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            solicitorSubmitApplication.aboutToSubmit(caseDetails, beforeCaseDetails);

        assertThat(response.getErrors()).containsExactly("Account balance insufficient");
    }

    private void mockExpectedCaseDetails(CaseDetails<CaseData, State> caseDetails, CaseData caseData, State state) {
        final CaseDetails<CaseData, State> expectedCaseDetails = new CaseDetails<>();
        expectedCaseDetails.setData(caseData);
        expectedCaseDetails.setId(TEST_CASE_ID);
        expectedCaseDetails.setState(state);

        when(submissionService.submitApplication(caseDetails)).thenReturn(expectedCaseDetails);
    }
}
