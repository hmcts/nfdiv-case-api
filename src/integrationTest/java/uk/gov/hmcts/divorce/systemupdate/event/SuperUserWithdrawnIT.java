package uk.gov.hmcts.divorce.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLICATION_WITHDRAWN;
import static uk.gov.hmcts.divorce.systemupdate.event.SuperuserWithdrawn.SUPERUSER_WITHDRAWN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    IdamWireMock.PropertiesInitializer.class
})
public class SuperUserWithdrawnIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void givenWithdrawEventWhenAboutToSubmitCallbackIsInvokedRemoveApplicants() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CaseData caseData = validCaseDataForIssueApplication();
        caseData.setDivorceUnit(Court.SERVICE_CENTRE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUPERUSER_WITHDRAWN)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            );

        verify(ccdAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_2.getRole()
            )
        ));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(CITIZEN_APPLICATION_WITHDRAWN),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(CITIZEN_APPLICATION_WITHDRAWN),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenWithdrawEventWhenAboutToSubmitCallbackIsInvokedSendWelshNotifications() throws Exception {
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        CaseData caseData = validCaseDataForIssueApplication();
        caseData.setDivorceUnit(Court.SERVICE_CENTRE);

        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUPERUSER_WITHDRAWN)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            );

        verify(ccdAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_2.getRole()
            )
        ));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(CITIZEN_APPLICATION_WITHDRAWN),
                anyMap(),
                eq(WELSH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(CITIZEN_APPLICATION_WITHDRAWN),
                anyMap(),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }
}
