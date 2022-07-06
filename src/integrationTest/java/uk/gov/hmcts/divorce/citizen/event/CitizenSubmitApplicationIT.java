package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSubmitApplication.CITIZEN_SUBMIT;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesNotFound;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    FeesWireMock.PropertiesInitializer.class,
})
public class CitizenSubmitApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        FeesWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        FeesWireMock.stopAndReset();
    }

    @Test
    public void givenValidCaseDataThenReturnResponseWithNoErrors() throws Exception {
        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(validApplicant1CaseData(), CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse()));
    }

    @Test
    public void givenValidCaseDataWithHwfThenSendEmailsToApplicant1AndReturnResponseWithNoErrors() throws Exception {
        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson());

        CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);
        caseData.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(OUTSTANDING_ACTIONS), anyMap(), eq(ENGLISH));

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackWithHwfSuccessfulResponse()));
    }

    @Test
    public void givenFeeEventIsNotAvailableWhenAboutToStartCallbackIsInvokedThenReturn404FeeEventNotFound()
        throws Exception {
        stubForFeesNotFound();

        CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setApplicationFeeOrderSummary(OrderSummary.builder().paymentTotal("55000").build());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isNotFound()
            )
            .andExpect(
                result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.NotFound.class)
            )
            .andExpect(
                result -> assertThat(requireNonNull(result.getResolvedException()).getMessage())
                    .contains("404 Fee event not found")
            );
    }

    @Test
    public void givenInvalidCaseDataThenReturnResponseWithErrors() throws Exception {
        var data = invalidCaseData();
        data.getApplicant2().setEmail("onlineApplicant2@email.com");

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_SUBMIT, Draft.name())))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCcdAboutToStartCallbackErrorResponse()));
    }

    @Test
    public void givenRequestBodyIsNullWhenEndpointInvokedThenReturnBadRequest() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenValidJointCaseDataThenReturnResponseWithNoErrors() throws Exception {
        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(validApplicant2CaseData(), CITIZEN_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // marriageDate and payments.id are ignored using ${json-unit.ignore}
        // assertion will fail if the above elements are missing actual value
        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulForJointApplicationResponse()));
    }

    @Test
    public void givenInvalidJointCaseDataThenReturnResponseWithErrors() throws Exception {
        var data = validApplicant1CaseData();
        data.getApplicant2().setEmail("test@email.com");
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_SUBMIT, Draft.name())))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCcdAboutToStartCallbackErrorForJointApplicationResponse()));
    }

    @Test
    public void givenApplicant2OfflineInvalidJointCaseDataThenReturnResponseWithErrors() throws Exception {
        var data = validApplicant1CaseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(data, CITIZEN_SUBMIT, Draft.name())))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedCcdAboutToSubmitCallbackErrorForApplicant2OfflineJointApplicationResponse()));
    }

    private String expectedCcdAboutToStartCallbackErrorResponse() throws IOException {
        File invalidCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-statement-of-truth-errors.json");

        return new String(Files.readAllBytes(invalidCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-statement-of-truth.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackWithHwfSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-help-with-fees.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackSuccessfulForJointApplicationResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-joint-application.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToStartCallbackErrorForJointApplicationResponse() throws IOException {
        File invalidCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-start-joint-application-errors.json");

        return new String(Files.readAllBytes(invalidCaseDataJsonFile.toPath()));
    }

    private String expectedCcdAboutToSubmitCallbackErrorForApplicant2OfflineJointApplicationResponse() throws IOException {
        File invalidCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/applicant-1-about-to-submit-joint-application-applicant-2-offline-errors.json");

        return new String(Files.readAllBytes(invalidCaseDataJsonFile.toPath()));
    }
}
