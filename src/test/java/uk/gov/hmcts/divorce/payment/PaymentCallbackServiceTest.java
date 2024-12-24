package uk.gov.hmcts.divorce.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.divorce.common.service.task.UpdateSuccessfulPaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.callback.PaymentCallbackDto;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class PaymentCallbackServiceTest {
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

    @InjectMocks
    private PaymentCallbackService paymentCallbackService;

    @Test
    public void shouldNotProcessCallbackIfPaymentNotSuccessful() {
        PaymentCallbackDto callback = PaymentCallbackDto.builder()
                .status(PaymentStatus.CANCELLED.toString())
                .build();

        paymentCallbackService.handleCallback(callback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    public void shouldNotProcessCallbackIfCaseNotAwaitingPayment() {
        PaymentCallbackDto callback = PaymentCallbackDto.builder()
            .status(PaymentStatus.SUCCESS.toString())
            .ccdCaseNumber(TEST_CASE_ID.toString())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .state(State.Submitted.toString())
            .build();

        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.getCase(user.getAuthToken(), TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID.toString()))
            .thenReturn(caseDetails);

        paymentCallbackService.handleCallback(callback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    public void shouldProcessCitizenApplicationPaymentCallback() {
        PaymentCallbackDto callback = PaymentCallbackDto.builder()
            .status(PaymentStatus.SUCCESS.toString())
            .ccdCaseNumber(TEST_CASE_ID.toString())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .state(State.AwaitingPayment.toString())
            .build();

        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.getCase(user.getAuthToken(), TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID.toString()))
            .thenReturn(caseDetails);

        paymentCallbackService.handleCallback(callback);

        verify(ccdUpdateService).submitEventWithRetry(
            TEST_CASE_ID.toString(),
            CITIZEN_PAYMENT_MADE,
            updateSuccessfulPaymentStatus,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }

    @Test
    public void shouldProcessCitizenFinalOrderPaymentCallback() {
        PaymentCallbackDto callback = PaymentCallbackDto.builder()
            .status(PaymentStatus.SUCCESS.toString())
            .ccdCaseNumber(TEST_CASE_ID.toString())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .state(State.AwaitingFinalOrderPayment.toString())
            .build();

        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApi.getCase(user.getAuthToken(), TEST_SERVICE_AUTH_TOKEN, TEST_CASE_ID.toString()))
            .thenReturn(caseDetails);

        paymentCallbackService.handleCallback(callback);

        verify(ccdUpdateService).submitEventWithRetry(
            TEST_CASE_ID.toString(),
            RESPONDENT_FINAL_ORDER_PAYMENT_MADE,
            updateSuccessfulPaymentStatus,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }
}
