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
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.CitizenFinalOrderDelayReason.CITIZEN_FINAL_ORDER_DELAY_REASON;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenFinalOrderDelayReasonIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private NotificationService notificationService;

    @Test
    void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicant1() throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData data = caseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_FINAL_ORDER_DELAY_REASON, "finalOrderOverdue")))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailInWelshToApplicant1() throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData data = caseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_FINAL_ORDER_DELAY_REASON, "finalOrderOverdue")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(WELSH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicant2() throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        final CaseData data = caseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_FINAL_ORDER_DELAY_REASON, "finalOrderOverdue")))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailInWelshToApplicant2() throws Exception {
        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        final CaseData data = caseData();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.setFinalOrder(FinalOrder.builder().dateFinalOrderNoLongerEligible(getExpectedLocalDate().plusDays(30)).build());
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_FINAL_ORDER_DELAY_REASON, "finalOrderOverdue")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(WELSH));
        verifyNoMoreInteractions(notificationService);
    }
}
