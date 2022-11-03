package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.service.task.ProgressFinalOrderState;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.ApplyForFinalOrder.FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ApplyForFinalOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProgressFinalOrderState progressFinalOrderState;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldReturnErrorWhenApplyForFinalOrderIsNoAndMidEventIsInvoked() throws Exception {

        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicant1WantToApplyForFinalOrder(NO);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=SolicitorApplyForFinalOrder")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, FINAL_ORDER_REQUESTED)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").value("You must select 'Yes' to apply for Final Order"));
    }

    @Test
    void shouldNotReturnErrorWhenApplyForFinalOrderIsYesAndMidEventIsInvoked() throws Exception {

        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicant1WantToApplyForFinalOrder(YES);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=SolicitorApplyForFinalOrder")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, FINAL_ORDER_REQUESTED)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    void shouldSendEmailToApplicant1InAwaitingFinalOrderStateInSoleApplication() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = caseData();
        data.getApplication().setPreviousState(AwaitingFinalOrder);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "AwaitingFinalOrder")))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailInWelshToApplicantInAwaitingFinalOrderStateInSoleApplication() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = caseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());
        data.getApplication().setPreviousState(AwaitingFinalOrder);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(WELSH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailsToApplicant1InFinalOrderOverdueState() throws Exception {

        final CaseData data = caseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().minusDays(30)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "finalOrderOverdue")))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndApplicant2HasNotAppliedForFinalOrderYet() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = validJointApplicant1CaseData();
        data.getApplication().setPreviousState(AwaitingFinalOrder);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("App2 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant1SolicitorNotificationWhenJointApplicationAndApplicant1HasAppliedForFinalOrder() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = validJointApplicant1CaseData();
        data.getApplication().setPreviousState(AwaitingFinalOrder);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("App1 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("App2 Sol")
            .reference("12344")
            .email("app2sol@email.com")
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderSubmitted(LocalDateTime.of(2022, 9, 10, 1, 0))
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq("app2sol@email.com"), eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationToApplicant2IfJointApplicationAndApplicant2HaveAppliedForFinalOrder2ndInTime() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "FinalOrderRequested")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToApplicant1AndApplicant2WhenJointApplication() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = validJointApplicant1CaseData();
        data.getApplication().setPreviousState(AwaitingFinalOrder);
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailToApplicant1AndApplicant2InWelshWhenApplicant1AppliedForFOFirstInTimeJointApplication() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        data.getApplication().setPreviousState(AwaitingFinalOrder);

        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_ONE_APPLICANT_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(WELSH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(WELSH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationInWelshToApplicant2IfJointApplicationAndApplicant2HaveAppliedForFinalOrder2ndInTime() throws Exception {

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.setFinalOrder(FinalOrder.builder()
            .dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30))
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .build());
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        data.getApplication().setPreviousState(AwaitingJointFinalOrder);
        data.getApplication().setWelshPreviousState(FinalOrderRequested);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, FINAL_ORDER_REQUESTED, "WelshTranslationReview")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_BOTH_APPLICANTS_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(WELSH));

        verifyNoMoreInteractions(notificationService);
    }
}
