package uk.gov.hmcts.divorce.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.OnlinePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto.PaymentDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.payment.rule.ApplicationPaymentMadeRule;
import uk.gov.hmcts.divorce.payment.rule.PaymentMadeRuleEngine;
import uk.gov.hmcts.divorce.payment.service.PaymentCallbackService;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;

@ExtendWith(MockitoExtension.class)
class PaymentCallbackServiceTest {
    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResponseEntity<CreditAccountPaymentResponse> responseEntity;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CoreCaseDataApi ccdApi;

    @Mock
    private UpdateSuccessfulPaymentStatus updateSuccessfulPaymentStatus;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private PaymentMadeRuleEngine paymentMadeRuleEngine;

    @InjectMocks
    private PaymentCallbackService paymentCallbackService;

    private User user;

    @BeforeEach
    void setup() {
        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);
    }

    @Test
    void shouldNotProcessCallbackIfPaymentUnsuccessful() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .state(AwaitingPayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.NOT_PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotProcessCallbackIfPaymentMethodWasPBA() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder().build())
            .state(AwaitingPayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(
            ServiceRequestStatus.PAID, OnlinePaymentMethod.PAYMENT_BY_ACCOUNT
        );

        processPaymentCallback(caseDetails, paymentCallback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotProcessCallbackIfCaseNotAwaitingPayment() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder().build())
            .state(Submitted)
            .build();
        CaseData caseData = caseDetails.getData();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        when(paymentMadeRuleEngine.find(Submitted, paymentCallback.getServiceRequestReference(), caseData))
            .thenReturn(Optional.empty());

        processPaymentCallback(caseDetails, paymentCallback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldProcessPaymentCallbackIfMatchingPaymentMadeRuleIsFound() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .application(Application.builder().applicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE).build())
                .build()
            ).state(AwaitingPayment)
            .build();
        CaseData caseData = caseDetails.getData();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        when(paymentMadeRuleEngine.find(AwaitingPayment, paymentCallback.getServiceRequestReference(), caseData))
            .thenReturn(Optional.of(new ApplicationPaymentMadeRule()));

        processPaymentCallback(caseDetails, paymentCallback);

        verify(ccdUpdateService).submitEventWithRetry(
            eq(TEST_CASE_ID.toString()),
            eq(CITIZEN_PAYMENT_MADE),
            any(UpdateSuccessfulPaymentStatus.class),
            eq(user),
            eq(TEST_SERVICE_AUTH_TOKEN)
        );
    }

    private void processPaymentCallback(
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails,
        PaymentCallbackDto paymentCallback
    ) {
        CaseDetails reformCaseDetails = spy(CaseDetails.builder().build());

        lenient().when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        lenient().when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        lenient().when(ccdApi.getCase(user.getAuthToken(), TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID.toString()))
            .thenReturn(reformCaseDetails);
        lenient().when(caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails))
            .thenReturn(caseDetails);

        paymentCallbackService.handleCallback(paymentCallback);
    }

    private PaymentCallbackDto buildPaymentCallback(
        ServiceRequestStatus serviceRequestStatus,
        OnlinePaymentMethod paymentMethod
    ) {
        return PaymentCallbackDto.builder()
            .serviceRequestStatus(serviceRequestStatus)
            .serviceRequestReference(TEST_SERVICE_REFERENCE)
            .payment(PaymentDto.builder().paymentMethod(paymentMethod).build())
            .ccdCaseNumber(TEST_CASE_ID.toString())
            .build();
    }
}
