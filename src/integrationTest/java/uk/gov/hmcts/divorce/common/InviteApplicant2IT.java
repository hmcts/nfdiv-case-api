package uk.gov.hmcts.divorce.common;

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
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.divorce.common.event.InviteApplicant2.INVITE_APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_APPLICANT1_REPRESENTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_SOLICITOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class InviteApplicant2IT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final boolean IS_REPRESENTED = true;

    @Autowired
    private MockMvc mockMvc;

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
    public void givenValidCaseDataWhenCallbackIsInvokedThenGenerateAccessCodeAndSendEmailToApplicant1AndApplicant2() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, INVITE_APPLICANT_2)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse(!IS_REPRESENTED)));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenEnsureCorrectEmailSentToApplicant2WhenApplicant1Represented() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .organisationPolicy(
                OrganisationPolicy.<UserRole>builder()
                    .organisation(
                        Organisation.builder()
                            .organisationName(TEST_SOLICITOR_FIRM_NAME)
                            .build())
                    .build())
            .build());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, INVITE_APPLICANT_2)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse(!IS_REPRESENTED)));

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_APPLICANT1_REPRESENTED),
                anyMap(),
                eq(ENGLISH),
                anyLong()
            );
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicant2SolicitorWhenApplicant2IsRepresented() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setSolicitor(Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build());
        data.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, INVITE_APPLICANT_2)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse(IS_REPRESENTED)));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICANT1_ANSWERS_SENT_FOR_REVIEW), anyMap(), eq(ENGLISH), anyLong());


        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(JOINT_APPLICANT2_ANSWERS_SENT_FOR_REVIEW_SOLICITOR), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponse(boolean isRepresented) throws IOException {

        File validCaseDataJsonFile;

        if (isRepresented) {
            validCaseDataJsonFile = getFile(
                "classpath:wiremock/responses/about-to-submit-invite-applicant-2-represented.json");

        } else {
            validCaseDataJsonFile = getFile(
                "classpath:wiremock/responses/about-to-submit-invite-applicant-2.json");
        }

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }
}
