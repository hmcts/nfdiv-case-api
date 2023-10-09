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
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant1Resubmit.APPLICANT_1_RESUBMIT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class Applicant1ResubmitIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicant1AndApplicant2() throws Exception {
        CaseData data = validApplicant2CaseData();
        var marriageDetails = data.getApplication().getMarriageDetails();
        marriageDetails.setDate(LocalDate.of(2020, 1, 1));
        data.setNote("Marriage date incorrect, correct date: 1/1/2020");
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, APPLICANT_1_RESUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailInWelshToApplicant1AndApplicant2() throws Exception {
        CaseData data = validApplicant2CaseData();
        var marriageDetails = data.getApplication().getMarriageDetails();
        marriageDetails.setDate(LocalDate.of(2020, 1, 1));
        data.setNote("Marriage date incorrect, correct date: 1/1/2020");
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, APPLICANT_1_RESUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponseInWelsh()));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
                argThat(
                    anyOf(
                        hasEntry(PARTNER, "gŵr")
                    )
                ),
                eq(WELSH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
                argThat(
                    anyOf(
                        hasEntry(PARTNER, "gwraig")
                    )
                ),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicant1AndApplicant2Solicitor() throws Exception {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build()
        );
        var marriageDetails = data.getApplication().getMarriageDetails();
        marriageDetails.setDate(LocalDate.of(2020, 1, 1));
        data.setNote("Marriage date incorrect, correct date: 1/1/2020");
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, APPLICANT_1_RESUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE), anyMap(), eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenInvalidCaseDataThenReturnResponseWithErrors() throws Exception {
        CaseData data = invalidCaseData();
        data.getApplicant2().setEmail("onlineApplicant2@email.com");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, APPLICANT_1_RESUBMIT)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackErrorResponse()));
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-resubmit-application.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponseInWelsh() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-resubmit-application-notification-in-welsh.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackErrorResponse() throws IOException {
        File invalidCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-resubmit-application-errors.json");

        return new String(Files.readAllBytes(invalidCaseDataJsonFile.toPath()));
    }
}
