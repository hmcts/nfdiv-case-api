package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyUnauthorized;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {DocAssemblyWireMock.PropertiesInitializer.class})
public class CaseworkerIssueApplicationTest {

    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP =
        "classpath:caseworker-issue-application-about-to-submit-app2-sol-rep-response.json";
    private static final String ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP =
        "classpath:caseworker-issue-application-about-to-submit-app2-not-sol-rep-response.json";
    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR =
        "classpath:caseworker-issue-application-about-to-submit-error-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private DocumentIdProvider documentIdProvider;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        when(clock.instant()).thenReturn(Instant.parse("2021-06-17T12:00:00.000Z"));
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void shouldGenerateMiniApplicationAndRespondentAosAndSetIssueDateWhenRespondentIsSolicitorRepresented() throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().name("testsol").isDigital(YES).build());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application").thenReturn("Respondent Invitation");

        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "Divorce_CP_Mini_Application_Sole_Joint.docx");
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "Divorce_CP_Dummy_Template.docx");

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ISSUE_APPLICATION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP))
            );
    }

    @Test
    void shouldGenerateOnlyMiniApplicationAndSetIssueDateWhenRespondentIsNotSolicitorRepresented() throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application");

        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "Divorce_CP_Mini_Draft_Application_Template.docx");

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ISSUE_APPLICATION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP))
            );
    }

    @Test
    void givenInvalidCaseDataWhenAboutToSubmitCallbackIsInvokedThenResponseContainsErrors() throws Exception {

        final CaseData caseData = invalidCaseData();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application");

        stubForDocAssembly();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ISSUE_APPLICATION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR))
            );
    }

    @Test
    void givenValidCaseDataWhenAuthorizationFailsForDocAssemblyThenStatusIsUnauthorized() throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application");

        stubForDocAssemblyUnauthorized();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_ISSUE_APPLICATION)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            );
    }
}
