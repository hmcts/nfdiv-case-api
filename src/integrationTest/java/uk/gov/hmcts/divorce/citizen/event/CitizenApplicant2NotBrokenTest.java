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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.service.notify.NotificationClientException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2NotBroken.APPLICANT_2_NOT_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_REJECTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_REJECTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getJointApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseDataMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenApplicant2NotBrokenTest {

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
        CaseData data = validJointApplicant1CaseDataMap();

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, APPLICANT_2_NOT_BROKEN)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICANT1_APPLICANT2_REJECTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(JOINT_APPLICANT2_APPLICANT2_REJECTED), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenSendEmailThrowsExceptionWhenCallbackIsInvokedThenReturnBadRequest() throws Exception {

        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_REJECTED),
            anyMap(),
            eq(ENGLISH));

        doThrow(new NotificationException(new NotificationClientException("All template params not passed")))
            .when(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_REJECTED),
            anyMap(),
            eq(ENGLISH));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(validJointApplicant1CaseDataMap(), APPLICANT_2_NOT_BROKEN)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().string("All template params not passed"));
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/about-to-submit-applicant-2-not-broken.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

}
