package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.TestResourceUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGrantFinalOrder.CASEWORKER_GRANT_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED_COVER_LETTER_APP_2;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANTS_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyUnauthorized;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataForGrantFinalOrder;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class
})
public class CaseworkerGrantFinalOrderIT {

    public static final String GRANT_FINAL_ORDER_RESPONSE_JSON = "classpath:caseworker-grant-final-order-response.json";
    public static final String GRANT_FINAL_ORDER_OFFLINE_RESPONSE_JSON = "classpath:caseworker-grant-final-order-offline-response.json";

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

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.FEBRUARY, 15, 13, 39);
        Instant instant = dateTime.atZone(ZoneId.of("Europe/London")).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Final-Order-Granted.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse(GRANT_FINAL_ORDER_RESPONSE_JSON)));

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentAndFinalOrderCoverLetterWhenAboutToSubmitCallbackIsInvokedForOfflineCase()
        throws Exception {

        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setEmail(null);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Final-Order-Granted.docx");
        stubForDocAssemblyWith("a11dc4a5-30b0-4a91-8fbb-1676cd300421", "FL-NFD-GOR-ENG-Final-Order-Cover-Letter.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_GRANT_FINAL_ORDER)
                )
            )
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse(GRANT_FINAL_ORDER_OFFLINE_RESPONSE_JSON)));

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldGenerateGrantFinalOrderDocumentInWelshAndUpdateCaseDataWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-WEL-Final-Order-Granted.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(TestResourceUtil.expectedResponse(GRANT_FINAL_ORDER_RESPONSE_JSON));
        jsonDocument.set("data.applicant1LanguagePreferenceWelsh", "Yes");

        assertThatJson(response)
            .isEqualTo(jsonDocument.json());

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldFailValidationWhenAboutToSubmitCallbackIsInvokedAndNotYetEligibleFinalOrder() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getFinalOrder().setDateFinalOrderEligibleFrom(LocalDate.of(2022, 3, 1));

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
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.errors").value("Case is not yet eligible for Final Order"));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldReturn401UnauthorizedWhenAboutToSubmitCallbackIsInvokedAndAuthorizationFailsForDocAssembly() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyUnauthorized();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            );
    }

    @Test
    public void shouldGenerateFinalOrderAndSendNotificationToSolicitorsWhenAboutToSubmitCallbackIsInvoked() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .email(TEST_SOLICITOR_EMAIL)
                .reference("App1 Sol Ref")
                .build()
        );

        final Applicant applicant2 = getApplicant();
        applicant2.setSolicitorRepresented(YES);
        applicant2.setSolicitor(Solicitor.builder()
            .name("App2 Sol")
            .email(TEST_USER_EMAIL)
            .reference("App2 Sol Ref")
            .build());
        caseData.setApplicant2(applicant2);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Final-Order-Granted.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse("classpath:caseworker-grant-final-order-solicitor-response.json")));

        verifyNoInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToSolicitorsWhenSubmittedCallbackIsInvoked() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .email(TEST_SOLICITOR_EMAIL)
                .reference("App1 Sol Ref")
                .build()
        );

        final Applicant applicant2 = getApplicant();
        applicant2.setSolicitorRepresented(YES);
        applicant2.setSolicitor(Solicitor.builder()
            .name("App2 Sol")
            .email(TEST_USER_EMAIL)
            .reference("App2 Sol Ref")
            .build());
        caseData.setApplicant2(applicant2);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLICITOR_FINAL_ORDER_GRANTED),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLICITOR_FINAL_ORDER_GRANTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationToApplicantAndRespondentWhenSubmittedCallbackIsInvokedForASoleCase() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));
        caseData.getApplicant1().setEmail("applicant@email.com");

        final Applicant applicant2 = getApplicant();
        applicant2.setEmail("respondent@email.com");
        caseData.setApplicant2(applicant2);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_GRANT_FINAL_ORDER)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq("applicant@email.com"),
                eq(APPLICANTS_FINAL_ORDER_GRANTED),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq("respondent@email.com"),
                eq(APPLICANTS_FINAL_ORDER_GRANTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendWelshNotificationToApplicantAndRespondentWhenSubmittedCallbackIsInvokedForASoleCase() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));
        caseData.getApplicant1().setEmail("applicant@email.com");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        final Applicant applicant2 = getApplicant();
        applicant2.setEmail("respondent@email.com");
        applicant2.setLanguagePreferenceWelsh(YES);
        caseData.setApplicant2(applicant2);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_GRANT_FINAL_ORDER)
                )
            )
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq("applicant@email.com"),
                eq(APPLICANTS_FINAL_ORDER_GRANTED),
                anyMap(),
                eq(WELSH));

        verify(notificationService)
            .sendEmail(
                eq("respondent@email.com"),
                eq(APPLICANTS_FINAL_ORDER_GRANTED),
                anyMap(),
                eq(WELSH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendBulkPrintNotificationToOfflineApplicantAndOfflineRespondentWhenSubmittedCallbackIsInvoked() throws Exception {
        final CaseData caseData = buildCaseDataForGrantFinalOrder(SOLE_APPLICATION, DIVORCE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 4, 28));
        caseData.getApplicant1().setOffline(YES);

        final Applicant applicant2 = getApplicant();
        applicant2.setOffline(YES);
        applicant2.setEmail(null);
        caseData.setApplicant2(applicant2);

        final ListValue<DivorceDocument> finalOrderGrantedLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(FINAL_ORDER_GRANTED)
                    .build())
                .build();

        final ListValue<ConfidentialDivorceDocument> applicant1FinalOrderGrantedCoverLetter =
            ListValue.<ConfidentialDivorceDocument>builder()
                .value(ConfidentialDivorceDocument.builder()
                    .confidentialDocumentsReceived(ConfidentialDocumentsReceived.FINAL_ORDER_GRANTED_COVER_LETTER_APP_1)
                    .build())
                .build();

        final ListValue<DivorceDocument> applicant2FinalOrderGrantedCoverLetter =
            ListValue.<DivorceDocument>builder()
                .value(DivorceDocument.builder()
                    .documentType(FINAL_ORDER_GRANTED_COVER_LETTER_APP_2)
                    .build())
                .build();

        caseData.getDocuments().setDocumentsGenerated(
            List.of(finalOrderGrantedLetter, applicant2FinalOrderGrantedCoverLetter)
        );

        caseData.getDocuments().setConfidentialDocumentsGenerated(
            List.of(applicant1FinalOrderGrantedCoverLetter)
        );

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    CASEWORKER_GRANT_FINAL_ORDER)
                )
            )
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(bulkPrintService, times(2)).print(any());
        verifyNoMoreInteractions(bulkPrintService);

        verifyNoInteractions(notificationService);
    }
}
