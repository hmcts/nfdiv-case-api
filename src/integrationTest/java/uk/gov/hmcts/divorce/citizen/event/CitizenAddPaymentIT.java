package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationOutstandingActionNotification;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationSubmittedNotification;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.hmcts.divorce.payment.model.Payment;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDateTime;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenAddPayment.CITIZEN_ADD_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.CANCELLED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.DECLINED;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.IN_PROGRESS;
import static uk.gov.hmcts.divorce.payment.model.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.jointCaseDataWithOrderSummary;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenAddPaymentIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ApplicationSubmittedNotification notification;

    @Autowired
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ApplicationOutstandingActionNotification outstandingActionNotification;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    public void givenLastAddedPaymentIsInProgress() throws Exception {
        CaseData data = caseDataWithOrderSummary();
        data.getApplication().setDateSubmitted(LocalDateTime.now());
        data.getApplication().setSolSignStatementOfTruth(YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder()
            .amount(55000)
            .status(IN_PROGRESS)
            .build();

        data.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CITIZEN_ADD_PAYMENT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenLastAddedPaymentWasCanceled() throws Exception {
        CaseData data = caseDataWithOrderSummary();
        data.getApplication().setDateSubmitted(LocalDateTime.now());
        data.getApplication().setSolSignStatementOfTruth(YES);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder()
            .amount(55000)
            .status(CANCELLED)
            .build();

        data.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CITIZEN_ADD_PAYMENT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenValidSoleCaseDataWhenCallbackIsInvokedThenSendEmailsToApplicant1() throws Exception {
        CaseData data = caseDataWithOrderSummary();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);
        data.getApplication().setDateSubmitted(LocalDateTime.now());

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder()
            .amount(55000)
            .status(SUCCESS)
            .build();

        data.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CITIZEN_ADD_PAYMENT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICATION_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(OUTSTANDING_ACTIONS), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidJointCaseDataWhenCallbackIsInvokedThenSendFourEmails() throws Exception {
        CaseData data = jointCaseDataWithOrderSummary();
        data.getApplication().setDateSubmitted(LocalDateTime.now());
        data.getApplication().getMarriageDetails().setMarriedInUk(NO);
        data.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(MARRIAGE_CERTIFICATE, MARRIAGE_CERTIFICATE_TRANSLATION));
        data.getApplication().setApplicant2CannotUploadSupportingDocument(Set.of(NAME_CHANGE_EVIDENCE));
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder()
            .amount(55000)
            .status(SUCCESS)
            .build();

        data.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CITIZEN_ADD_PAYMENT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICATION_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(APPLICATION_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(OUTSTANDING_ACTIONS), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(OUTSTANDING_ACTIONS), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenDeclinedPaymentDontSendNotification() throws Exception {
        CaseData data = caseDataWithOrderSummary();
        data.getApplication().setDateSubmitted(LocalDateTime.now());

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder()
            .amount(55000)
            .status(DECLINED)
            .build();

        data.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CITIZEN_ADD_PAYMENT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenSendEmailThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() throws Exception {
        CaseData data = caseDataWithOrderSummary();
        data.getApplication().setDateSubmitted(LocalDateTime.now());
        data.getApplication().setApplicant1StatementOfTruth(YES);
        data.getApplication().setSolSignStatementOfTruth(null);

        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();
        data.getApplication().setApplicationFeeOrderSummary(orderSummary);

        Payment payment = Payment.builder()
            .amount(55000)
            .status(SUCCESS)
            .build();

        data.getApplication().setApplicationPayments(singletonList(new ListValue<>("1", payment)));

        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICATION_SUBMITTED),
            anyMap(),
            eq(ENGLISH));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, CITIZEN_ADD_PAYMENT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("All template params not passed"));
    }
}
