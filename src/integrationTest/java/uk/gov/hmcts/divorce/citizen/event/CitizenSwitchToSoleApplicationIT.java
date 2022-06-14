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
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSole.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ENDED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenSwitchToSoleApplicationIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private IdamService idamService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedForApplicant1SwitchToSoleThenCaseIsWithdrawnAndNotificationsSent() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        setValidCaseInviteData(data);
        setupMocks(true);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE, "AwaitingApplicant2Response")))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccessfulResponse()));
        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICANT_SWITCH_TO_SOLE), anyMap(), eq(ENGLISH));
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICATION_ENDED), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedForApplicant2SwitchToSoleThenCaseIsWithdrawnAndNotificationsSent() throws Exception {
        CaseData data = validApplicant2CaseData();
        setValidCaseInviteData(data);
        setupMocks(false);


        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE, "Applicant2Approved")))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccessfulResponse()));
        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(APPLICANT_SWITCH_TO_SOLE), anyMap(), eq(ENGLISH));
        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICATION_ENDED), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedForApplicant1SwitchToSoleForUnlinkedApp2ThenAccessCodeSetToNull() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setCaseInvite(new CaseInvite(null, ACCESS_CODE, null));
        setValidCaseInviteData(data);
        setupMocks(true);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE, "AwaitingApplicant2Response")))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessCode").doesNotExist())
            .andReturn()
            .getResponse()
            .getContentAsString();


        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccessfulResponse()));
        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICANT_SWITCH_TO_SOLE), anyMap(), eq(ENGLISH));
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICATION_ENDED), anyMap(), eq(ENGLISH));
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWithWelshSelectedWhenCallbackIsInvokedThenSendEmailInWelsh() throws Exception {
        CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        setupMocks(true);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(caseData, SWITCH_TO_SOLE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2Email").doesNotExist())
            .andExpect(jsonPath("$.data.applicationType").value("soleApplication"));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(APPLICANT_SWITCH_TO_SOLE), anyMap(), eq(WELSH));
        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICATION_ENDED), anyMap(), eq(WELSH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidDataWhenCallbackIsInvokedForApp1SwitchToSoleThenCaseIsWithdrawnAndNotificationsSentInWelsh() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        setValidCaseInviteData(data);
        setupMocks(true);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE, "AwaitingApplicant2Response")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccessfulResponse()));
        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(APPLICANT_SWITCH_TO_SOLE),
                argThat(
                    anyOf(
                        hasEntry(PARTNER, "gŵr")
                    )
                ),
                eq(WELSH));
        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(JOINT_APPLICATION_ENDED),
                argThat(
                    anyOf(
                        hasEntry(PARTNER, "gwraig")
                    )
                ),
                eq(WELSH));
        verifyNoMoreInteractions(notificationService);
    }

    private String expectedCcdAboutToSubmitCallbackSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/about-to-submit-switch-to-sole-application.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private CaseData setValidCaseInviteData(CaseData caseData) {
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setCaseInvite(
            CaseInvite.builder()
                .applicant2InviteEmailAddress(TEST_APPLICANT_2_EMAIL)
                .applicant2UserId("app2")
                .build()
        );
        return caseData;
    }

    private void setupMocks(boolean isApplicant1) {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(isApplicant1);
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new User("system-user-token", UserDetails.builder().build()));
    }
}
