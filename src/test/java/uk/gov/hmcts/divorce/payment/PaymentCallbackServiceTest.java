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
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.model.CreditAccountPaymentResponse;
import uk.gov.hmcts.divorce.payment.model.OnlinePaymentMethod;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto;
import uk.gov.hmcts.divorce.payment.model.PaymentCallbackDto.PaymentDto;
import uk.gov.hmcts.divorce.payment.model.ServiceRequestStatus;
import uk.gov.hmcts.divorce.payment.service.PaymentCallbackService;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenGeneralApplicationPaymentMade.CITIZEN_GENERAL_APPLICATION_PAYMENT;
import static uk.gov.hmcts.divorce.citizen.event.CitizenPaymentMade.CITIZEN_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.citizen.event.CitizenServicePaymentMade.CITIZEN_SERVICE_PAYMENT;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
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
            .state(State.AwaitingPayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.NOT_PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotProcessCallbackIfPaymentMethodWasPBA() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder().build())
            .state(State.AwaitingPayment)
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
            .state(State.Submitted)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldProcessCitizenApplicationPaymentCallback() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .application(Application.builder().applicationFeeServiceRequestReference(TEST_SERVICE_REFERENCE).build())
                .build()
            ).state(State.AwaitingPayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verify(ccdUpdateService).submitEventWithRetry(
            TEST_CASE_ID.toString(),
            CITIZEN_PAYMENT_MADE,
            updateSuccessfulPaymentStatus,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }

    @Test
    void shouldProcessCitizenFinalOrderPaymentCallback() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .finalOrder(FinalOrder.builder().applicant2FinalOrderFeeServiceRequestReference(TEST_SERVICE_REFERENCE).build())
                .build()
            ).state(State.AwaitingFinalOrderPayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verify(ccdUpdateService).submitEventWithRetry(
            TEST_CASE_ID.toString(),
            RESPONDENT_FINAL_ORDER_PAYMENT_MADE,
            updateSuccessfulPaymentStatus,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }

    @Test
    void shouldProcessCitizenServicePaymentCallback() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .alternativeService(AlternativeService.builder().servicePaymentFee(
                    FeeDetails.builder()
                        .serviceRequestReference(TEST_SERVICE_REFERENCE)
                        .build()
                ).build()).build())
            .state(State.AwaitingServicePayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verify(ccdUpdateService).submitEventWithRetry(
            TEST_CASE_ID.toString(),
            CITIZEN_SERVICE_PAYMENT,
            updateSuccessfulPaymentStatus,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }

    @Test
    void shouldProcessCitizenSearchGovernmentRecordsPaymentCallback() {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder()
                .applicant1(Applicant.builder().generalAppServiceRequest(TEST_SERVICE_REFERENCE).build())
                .build()
            ).state(State.AwaitingGeneralApplicationPayment)
            .build();

        PaymentCallbackDto paymentCallback = buildPaymentCallback(ServiceRequestStatus.PAID, OnlinePaymentMethod.CARD);

        processPaymentCallback(caseDetails, paymentCallback);

        verify(ccdUpdateService).submitEventWithRetry(
            TEST_CASE_ID.toString(),
            CITIZEN_GENERAL_APPLICATION_PAYMENT,
            updateSuccessfulPaymentStatus,
            user,
            TEST_SERVICE_AUTH_TOKEN
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
