package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.model.PaymentItem;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.PaymentWireMock;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant2SolicitorApplyForFinalOrder.FINAL_ORDER_REQUESTED_APP2_SOL;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SolicitorPaymentMethod.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_2_SOLICITOR_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.buildServiceReferenceRequest;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreateServiceRequest;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP2_SOL_FO_PAYMENT_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    PaymentWireMock.PropertiesInitializer.class,
    FeesWireMock.PropertiesInitializer.class
})
public class Applicant2SolicitorApplyForFinalOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockitoBean
    private PbaService pbaService;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private NotificationService notificationService;

    @BeforeAll
    static void setUp() {
        PaymentWireMock.start();
        FeesWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        PaymentWireMock.stopAndReset();
        FeesWireMock.stopAndReset();
    }

    @Test
    public void createsOrderSummaryToPrepareCaseForPayment() throws Exception {
        var data = validCaseDataForAwaitingFinalOrder();

        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson(), EVENT_GENERAL, SERVICE_OTHER, KEYWORD_NOTICE);

        mockMvc.perform(post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED_APP2_SOL, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2SolFinalOrderFeeOrderSummary.PaymentTotal")
                .value("1000")
            )
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @Test
    public void createsServiceRequestToPrepareCaseForPayment() throws Exception {
        var data = caseDataWithOrderSummary();
        data.getFinalOrder().setApplicant2SolFinalOrderFeeOrderSummary(
            data.getApplication().getApplicationFeeOrderSummary()
        );
        data.getFinalOrder().setApplicant2SolPaymentHowToPay(FEE_PAY_BY_ACCOUNT);
        var serviceRequestBody = buildServiceReferenceRequest(data, data.getApplicant2().getFullName());
        serviceRequestBody.setFees(List.of(
            PaymentItem.builder()
                .ccdCaseNumber(TEST_CASE_ID.toString())
                .calculatedAmount("550")
                .code("FEE002")
                .build()
        ));

        when(pbaService.populatePbaDynamicList()).thenReturn(new DynamicList());
        stubCreateServiceRequest(OK, serviceRequestBody);

        mockMvc.perform(post(APP2_SOL_FO_PAYMENT_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED_APP2_SOL, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2FinalOrderFeeServiceRequestReference")
                .value(TEST_SERVICE_REFERENCE)
            )
            .andReturn()
            .getResponse()
            .getContentAsString();
    }

    @Test
    void shouldSendEmailToApplicant2InAwaitingFinalOrderStateInSoleApplication() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name(TEST_SOLICITOR_NAME)
            .email(TEST_SOLICITOR_EMAIL)
            .organisationPolicy(organisationPolicy())
            .build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());
        caseData.getFinalOrder().setFinalOrderReminderSentApplicant2(YES);

        caseData.getApplication().setIssueDate(LocalDate.now());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingFinalOrder);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, FINAL_ORDER_REQUESTED_APP2_SOL, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(APPLICANT_2_SOLICITOR_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }
}
