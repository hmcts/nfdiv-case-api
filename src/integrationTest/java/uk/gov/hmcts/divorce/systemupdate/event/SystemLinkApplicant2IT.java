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
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.testutil.CaseDataWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkApplicant2.SYSTEM_LINK_APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.CaseDataWireMock.stubForCitizenCcdCaseRoles;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CITIZEN_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_2_CITIZEN_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    IdamWireMock.PropertiesInitializer.class,
    CaseDataWireMock.PropertiesInitializer.class})
public class SystemLinkApplicant2IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
        CaseDataWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
        CaseDataWireMock.stopAndReset();
    }

    @Test
    public void givenValidAccessCodeWhenCallbackIsInvokedThenAccessCodeIsRemovedAndSolicitorRolesAreSet() throws Exception {
        CaseData data = caseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getCaseInvite().setAccessCode("D8BC9AQR");
        data.getCaseInvite().setApplicant2UserId("3");
        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, APP_2_CITIZEN_USER_ID, CITIZEN_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(CASEWORKER_AUTH_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(CASEWORKER_AUTH_TOKEN);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        stubForCitizenCcdCaseRoles();

        CallbackRequest callbackRequest = callbackRequest(data, SYSTEM_LINK_APPLICANT_2);
        callbackRequest.setCaseDetailsBefore(
            CaseDetails
                .builder()
                .data(
                    Map.of(
                    "applicant2UserId", "3",
                    "accessCode", "D8BC9AQR"
                    )
                )
                .build()
        );

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulResponse()));

        verify(serviceTokenGenerator).generate();
        verifyNoMoreInteractions(serviceTokenGenerator);
    }

    @Test
    public void givenNoApplicant2UserIdPassedWhenCallbackIsInvokedThen404ErrorIsReturned() throws Exception {
        CaseData data = caseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getCaseInvite().setAccessCode("D8BC9AQR");
        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, APP_2_CITIZEN_USER_ID, CITIZEN_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(CASEWORKER_AUTH_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(CASEWORKER_AUTH_TOKEN);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        stubForCitizenCcdCaseRoles();

        CallbackRequest callbackRequest = callbackRequest(data, SYSTEM_LINK_APPLICANT_2);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest))
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void givenNoCaseIdPassedWhenCallbackIsInvokedThen404ErrorIsReturned() throws Exception {
        CaseData data = caseData();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getCaseInvite().setAccessCode("D8BC9AQR");
        data.setDueDate(LocalDate.now().plus(2, ChronoUnit.WEEKS));

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, APP_2_CITIZEN_USER_ID, CITIZEN_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(CASEWORKER_AUTH_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(CASEWORKER_AUTH_TOKEN);

        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        stubForCitizenCcdCaseRoles();

        CallbackRequest callbackRequest = callbackRequest(data, SYSTEM_LINK_APPLICANT_2);
        callbackRequest.getCaseDetails().setId(null);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest))
            .accept(APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    private String expectedCcdAboutToStartCallbackSuccessfulResponse() throws IOException {
        File validCaseDataJsonFile = getFile(
            "classpath:wiremock/responses/about-to-submit-link-applicant-2.json");

        return new String(Files.readAllBytes(validCaseDataJsonFile.toPath()));
    }
}
