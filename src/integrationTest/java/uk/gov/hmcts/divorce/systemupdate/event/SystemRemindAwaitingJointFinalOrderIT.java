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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;

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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindAwaitingJointFinalOrder.SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SystemRemindAwaitingJointFinalOrderIT {

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
    public void shouldSendNotificationToApplicant2WhenJointFinalOrderIsAwaitingFromApplicant2() throws Exception {
        final CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.getFinalOrder().setDateFinalOrderSubmitted(LocalDateTime.now());
        caseData.getFinalOrder().setApplicant1AppliedForFinalOrderFirst(YesOrNo.YES);
        caseData.getFinalOrder().setApplicant2AppliedForFinalOrderFirst(YesOrNo.NO);
        caseData.getApplicant1().setEmail("applicant1@email.com");

        final Applicant applicant2 = getApplicant();
        applicant2.setEmail("applicant2@email.com");
        caseData.setApplicant2(applicant2);

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
                            SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq("applicant2@email.com"),
                eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApplicant1WhenJointFinalOrderIsAwaitingFromApplicant1() throws Exception {
        final CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.getFinalOrder().setDateFinalOrderSubmitted(LocalDateTime.now());
        caseData.getFinalOrder().setApplicant1AppliedForFinalOrderFirst(YesOrNo.NO);
        caseData.getFinalOrder().setApplicant2AppliedForFinalOrderFirst(YesOrNo.YES);
        caseData.getApplicant1().setEmail("applicant1@email.com");

        final Applicant applicant2 = getApplicant();
        applicant2.setEmail("applicant2@email.com");
        caseData.setApplicant2(applicant2);

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
                            SYSTEM_REMIND_AWAITING_JOINT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq("applicant1@email.com"),
                eq(JOINT_APPLICANT_OTHER_PARTY_APPLIED_FOR_FINAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }
}
