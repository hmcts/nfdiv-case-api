package uk.gov.hmcts.divorce.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
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

@ExtendWith(MockitoExtension.class)
class PaymentStatusServiceTest {

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
    @InjectMocks
    private PaymentStatusService paymentStatusService;

    private String reference;

    private uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails;

    @BeforeEach
    void setUp() {

        reference = UUID.randomUUID().toString();

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

        caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        caseDetails.setData(CaseData.builder().application(
                Application.builder().applicationPayments(payments).build())
            .build());
    }

    @Test
    void shouldReturnTrueIfCaseHasSuccessFulPayment() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", "")).build();
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(paymentClient.getPaymentByReference(any(), any(), eq(reference))).thenReturn(new uk.gov.hmcts.divorce.payment.model.Payment(
            "Success"));
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
    }

    @Test
    void shouldReturnFalseIfPaymentInProgress() {
        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", "")).build();
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
        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", "")).build();
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
        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", "")).build();
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
        final CaseDetails cd = CaseDetails.builder().data(Map.of("applicationPayments", "")).build();
        caseDetails.getData().getApplication().getApplicationPayments().get(0).getValue().setReference(null);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(same(cd))).thenReturn(caseDetails);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        paymentStatusService.hasSuccessFulPayment(List.of(cd));

        verify(idamService).retrieveSystemUpdateUserDetails();
        verify(user).getAuthToken();
        verify(authTokenGenerator).generate();
        verifyNoInteractions(paymentClient);
    }

}
