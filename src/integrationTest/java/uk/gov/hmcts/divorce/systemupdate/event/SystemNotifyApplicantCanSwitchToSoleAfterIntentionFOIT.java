package uk.gov.hmcts.divorce.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantCanSwitchToSoleAfterIntentionFO.SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SystemNotifyApplicantCanSwitchToSoleAfterIntentionFOIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void shouldSendNotificationToApp1SolicitorInJointCaseWhenApp1IntendsToSwitchToSole() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 10, 10));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApp2SolicitorInJointCaseWhenApp2IntendsToSwitchToSole() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 10, 10));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApp1InJointCaseWhenApp1IntendsToSwitchToSole() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 10, 10));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApp1InJointCaseWhenApp1IntendsToSwitchToSoleWelsh() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 10, 10));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
                anyMap(),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApp2InJointCaseWhenApp2IntendsToSwitchToSole() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 10, 10));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApp2InJointCaseWhenApp2IntendsToSwitchToSoleWelsh() throws Exception {
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 10, 10));
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .build());
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            SYSTEM_APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
                anyMap(),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }
}
