package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenResendInvite.CITIZEN_RESEND_INVITE;
import static uk.gov.hmcts.divorce.citizen.event.CitizenResendInviteTest.TO_LINK_TO_CASE_BY_OFFSET_DAYS;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenResendInviteIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @MockBean
    private NotificationService notificationService;

    @Test
    void applicationSentForReviewNotificationTriggeredWhenApplicant2IsNotRepresented() throws Exception {
        setMockClock(clock);

        final CaseData data = validApplicant1CaseData();
        data.setCaseInvite(data.getCaseInvite().generateAccessCode());
        data.setApplicationType(JOINT_APPLICATION);
        data.setDueDate(LocalDate.of(2021, 6, 20));

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, CITIZEN_RESEND_INVITE, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void correctFieldsChangedWhenApplicant2IsNotRepresented() throws Exception {
        setMockClock(clock);

        final CaseData data = validApplicant1CaseData();
        data.setCaseInvite(data.getCaseInvite().generateAccessCode());
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setApplicant2ReminderSent(YesOrNo.YES);

        final String originalAccessCode = data.getCaseInvite().accessCode();

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, CITIZEN_RESEND_INVITE, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .inPath("$.data.dueDate")
            .isString()
            .isEqualTo(LocalDate.now().plusDays(TO_LINK_TO_CASE_BY_OFFSET_DAYS).toString());

        assertThatJson(jsonStringResponse)
            .inPath("$.data.accessCode")
            .isString()
            .isNotEqualTo(originalAccessCode);

        assertThatJson(jsonStringResponse)
            .inPath("$.data.applicant2ReminderSent")
            .isAbsent();
    }

    @Test
    void applicationSentForReviewNotificationNotTriggeredWhenApplicant2IsRepresented() throws Exception {
        setMockClock(clock);

        final CaseData data = validApplicant1CaseData();
        data.getApplicant2().setSolicitorRepresented(YES);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, CITIZEN_RESEND_INVITE, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void caseInviteAndDueDateNotChangedWhenApplicant2IsRepresented() throws Exception {
        setMockClock(clock);

        final CaseData data = validApplicant1CaseData();
        data.getApplicant2().setSolicitorRepresented(YES);
        data.setCaseInvite(data.getCaseInvite().generateAccessCode());
        data.setApplicationType(JOINT_APPLICATION);
        data.setDueDate(LocalDate.of(2021, 6, 20));

        final String originalAccessCode = data.getCaseInvite().accessCode();
        final LocalDate originalDueDate = data.getDueDate();

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, CITIZEN_RESEND_INVITE, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .inPath("$.data.dueDate")
            .isString()
            .isEqualTo(originalDueDate.toString());

        assertThatJson(jsonStringResponse)
            .inPath("$.data.accessCode")
            .isString()
            .isEqualTo(originalAccessCode);
    }

    @Test
    void applicationSentForReviewNotificationNotTriggeredWhenCaseIsInvalid() throws Exception {
        setMockClock(clock);

        final CaseData data = validApplicant1CaseData();
        data.setCaseInvite(data.getCaseInvite().generateAccessCode());
        data.setApplicationType(SOLE_APPLICATION);

        final String jsonStringResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, CITIZEN_RESEND_INVITE, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .inPath("$.errors")
            .isArray()
            .isNotEmpty();

        verifyNoInteractions(notificationService);
    }
}
