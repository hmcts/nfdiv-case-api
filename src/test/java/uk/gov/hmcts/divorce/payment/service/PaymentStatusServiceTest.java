package uk.gov.hmcts.divorce.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.client.PaymentClient;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRejectCasesWithPaymentOverdue.APPLICATION_REJECTED_FEE_NOT_PAID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class PaymentStatusServiceTest {

    private static final String AWAITING_PAYMENT = "AwaitingPayment";
    private static final String APPLICATION_PAYMENTS = "applicationPayments";

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private User user;

    @Mock
    private UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private ServiceRequestSearchService serviceRequestSearchService;

    @InjectMocks
    private PaymentStatusService paymentStatusService;

    private String reference;

    private uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails;

    @BeforeEach
    void setUp() {

        reference = UUID.randomUUID().toString();

        caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        caseDetails.setData(CaseData.builder().application(
                Application.builder().applicationPayments(getPayments()).build())
            .build());

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(AwaitingPayment);
    }

    @Test
    void shouldReturnTrueIfCaseHasSuccessFulPayment() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, "")).state(AWAITING_PAYMENT).build();
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(paymentClient.getPaymentByReference(any(), any(), eq(reference)))
                .thenReturn(new uk.gov.hmcts.divorce.payment.model.Payment(SUCCESS.getLabel()));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldReturnFalseIfPaymentInProgress() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, "")).state(AWAITING_PAYMENT).build();
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(paymentClient.getPaymentByReference(any(), any(), eq(reference))).thenReturn(new uk.gov.hmcts.divorce.payment.model.Payment(
            "Created"));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldReturnFalseIfApplicationPaymentsIsNull() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, "")).state(AWAITING_PAYMENT).build();
        caseDetails.getData().getApplication().setApplicationPayments(null);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
        verifyNoInteractions(paymentClient);
    }

    @Test
    void shouldReturnFalseIfApplicationPaymentsIsEmpty() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, "")).state(AWAITING_PAYMENT).build();
        caseDetails.getData().getApplication().setApplicationPayments(emptyList());
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
        verifyNoInteractions(paymentClient);
    }

    @Test
    void shouldReturnFalseIfApplicationPaymentReferenceIsNull() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, "")).state(AWAITING_PAYMENT).build();
        caseDetails.getData().getApplication().getApplicationPayments().get(0).getValue().setReference(null);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
        verifyNoInteractions(paymentClient);
    }

    @Test
    void shouldTriggerPaymentEventIfAwaitingFinalOrderPaymentSuccessfulButInProgress() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of("finalOrderPayments", ""))
                .id(TEST_CASE_ID).state("AwaitingFinalOrderPayment").build();
        caseDetails.setState(AwaitingFinalOrderPayment);

        caseDetails.setData(CaseData.builder().finalOrder(
                        FinalOrder.builder().finalOrderPayments(getPayments()).build())
                .build());

        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(paymentClient.getPaymentByReference(TEST_SERVICE_AUTH_TOKEN, SERVICE_AUTHORIZATION, reference))
                .thenReturn(new uk.gov.hmcts.divorce.payment.model.Payment(SUCCESS.getLabel()));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails().getAuthToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
        verify(ccdUpdateService).submitEventWithRetry(TEST_CASE_ID.toString(),
                RESPONDENT_FINAL_ORDER_PAYMENT_MADE, updateSuccessfulPaymentStatus, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldTriggerPaymentEventIfAwaitingPaymentSuccessfulButInProgress() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, ""))
                .id(TEST_CASE_ID).state(AWAITING_PAYMENT).build();

        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(paymentClient.getPaymentByReference(TEST_SERVICE_AUTH_TOKEN, SERVICE_AUTHORIZATION, reference))
                .thenReturn(new uk.gov.hmcts.divorce.payment.model.Payment(SUCCESS.getLabel()));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails().getAuthToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
        verify(ccdUpdateService).submitEventWithRetry(TEST_CASE_ID.toString(),
                CITIZEN_PAYMENT_MADE, updateSuccessfulPaymentStatus, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldRejectCaseInStateAwaitingPaymentWhenPaymentOverdueAfter14Days() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, ""))
            .id(TEST_CASE_ID).state(AWAITING_PAYMENT).build();

        caseDetails.setLastModified(LocalDateTime.now().minusDays(15));
        paymentStatusService.processPaymentRejection(caseDetails, user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID,
            APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldRejectCaseInStateAwaitingPaymentWhenServiceRequestHasNotBeenPaidOrNotWithinGracePeriod() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, ""))
            .id(TEST_CASE_ID).state(AWAITING_PAYMENT).build();

        ServiceRequestDto serviceRequestDto = ServiceRequestDto.builder().payments(
            List.of(ServiceRequestDto.PaymentDto.builder().build())).serviceRequestStatus(ServiceRequestStatus.NOT_PAID).build();
        List<ServiceRequestDto> caseServiceRequests = List.of(serviceRequestDto);
        stubServiceRequestSearch(caseServiceRequests);

        caseDetails.setLastModified(LocalDateTime.now().minusDays(15));
        paymentStatusService.processPaymentRejection(caseDetails, user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID,
            APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldRejectCaseInStateAwaitingPaymentWhenNoPaymentMade() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, ""))
            .id(TEST_CASE_ID).state(AWAITING_PAYMENT).build();

        stubServiceRequestSearch(List.of(ServiceRequestDto.builder().serviceRequestStatus(ServiceRequestStatus.NOT_PAID).build()));

        caseDetails.setLastModified(LocalDateTime.now().minusDays(15));
        paymentStatusService.processPaymentRejection(caseDetails, user, SERVICE_AUTHORIZATION);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID,
            APPLICATION_REJECTED_FEE_NOT_PAID, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotRejectCaseInStateAwaitingPaymentWhenPaymentServiceRequestCreatedWithinLast24Hours() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of(APPLICATION_PAYMENTS, ""))
            .id(TEST_CASE_ID).state(AWAITING_PAYMENT).build();

        ServiceRequestDto serviceRequestDto = ServiceRequestDto.builder().payments(
            List.of(ServiceRequestDto.PaymentDto.builder().build())).dateCreated(Date.from(
                new Date().toInstant().minus(3, ChronoUnit.HOURS))).build();
        List<ServiceRequestDto> caseServiceRequests = List.of(serviceRequestDto);
        stubServiceRequestSearch(caseServiceRequests);

        caseDetails.setLastModified(LocalDateTime.now().minusDays(15));
        paymentStatusService.processPaymentRejection(caseDetails, user, SERVICE_AUTHORIZATION);

        verifyNoInteractions(ccdUpdateService);
    }

    private List<ListValue<Payment>> getPayments() {

        final Payment payment = Payment
                .builder()
                .status(PaymentStatus.IN_PROGRESS)
                .reference(reference)
                .build();

        final ListValue<Payment> paymentListValue = ListValue
                .<Payment>builder()
                .value(payment)
                .build();
        final List<ListValue<Payment>> payments = new ArrayList<>();
        payments.add(paymentListValue);

        return payments;
    }

    private void stubServiceRequestSearch(List<ServiceRequestDto> caseServiceRequests) {
        when(serviceRequestSearchService.getServiceRequestsForCase(caseDetails.getId()))
            .thenReturn(caseServiceRequests);
    }
}
