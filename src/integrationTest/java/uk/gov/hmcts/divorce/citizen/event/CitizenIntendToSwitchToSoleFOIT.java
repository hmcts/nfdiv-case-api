package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenIntendToSwitchToSoleFO.INTEND_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTEND_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.jointCaseDataWithOrderSummary;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenIntendToSwitchToSoleFOIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @Test
    void shouldSetApplicant1IntendsFieldsIfEventIsTriggeredByApplicant1()
        throws Exception {

        setMockClock(clock);

        CaseData data = validJointApplicant1CaseData();

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        data,
                        INTEND_SWITCH_TO_SOLE_FO,
                        "AwaitingJointFinalOrder")
                ))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .inPath("$.data.doesApplicant1IntendToSwitchToSole")
            .isEqualTo(YES);

        assertThatJson(actualResponse)
            .inPath("$.data.dateApplicant1DeclaredIntentionToSwitchToSoleFo")
            .isEqualTo(getExpectedLocalDate().toString());
    }

    @Test
    void shouldSetApplicant2IntendsFieldsIfEventIsTriggeredByApplicant2()
        throws Exception {

        setMockClock(clock);

        CaseData data = validJointApplicant1CaseData();

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);
        when(ccdAccessService.isApplicant2(anyString(), anyLong())).thenReturn(true);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(HttpHeaders.AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        data,
                        INTEND_SWITCH_TO_SOLE_FO,
                        "AwaitingJointFinalOrder")
                ))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .inPath("$.data.doesApplicant2IntendToSwitchToSole")
            .isEqualTo(YES);

        assertThatJson(actualResponse)
            .inPath("$.data.dateApplicant2DeclaredIntentionToSwitchToSoleFo")
            .isEqualTo(getExpectedLocalDate().toString());
    }

    @Test
    void shouldSendApplicant1IntendToSwitchToSoleFoNotificationsIfEventTriggeredByApplicant1() throws Exception {

        setMockClock(clock);

        final CaseData data = jointCaseDataWithOrderSummary();
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, INTEND_SWITCH_TO_SOLE_FO, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(INTEND_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant1IntendToSwitchToSoleFoWelshNotificationsIfEventTriggeredByApplicant1() throws Exception {

        setMockClock(clock);

        final CaseData data = jointCaseDataWithOrderSummary();
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, INTEND_SWITCH_TO_SOLE_FO, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(INTEND_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(WELSH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(WELSH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2IntendToSwitchToSoleFoNotificationsIfEventTriggeredByApplicant2() throws Exception {

        setMockClock(clock);

        final CaseData data = jointCaseDataWithOrderSummary();
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);
        when(ccdAccessService.isApplicant2(anyString(), anyLong())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, INTEND_SWITCH_TO_SOLE_FO, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(INTEND_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicant2IntendToSwitchToSoleFoWelshNotificationsIfEventTriggeredByApplicant2() throws Exception {

        setMockClock(clock);

        final CaseData data = jointCaseDataWithOrderSummary();
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);
        when(ccdAccessService.isApplicant2(anyString(), anyLong())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(data, INTEND_SWITCH_TO_SOLE_FO, String.valueOf(AwaitingApplicant2Response))))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(WELSH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(INTEND_TO_SWITCH_TO_SOLE_FO), anyMap(), eq(WELSH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }
}
