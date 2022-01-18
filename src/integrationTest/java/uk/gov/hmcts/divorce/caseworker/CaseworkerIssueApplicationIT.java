package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FilenameUtils;
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
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyUnauthorized;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock.stubDeleteFromDocumentManagementForSystem;
import static uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock.stubDownloadBinaryFromDocumentManagement;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SOLICITOR_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubSendLetters;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubStatusOfSendLetter;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    DocManagementStoreWireMock.PropertiesInitializer.class,
    SendLetterWireMock.PropertiesInitializer.class})
public class CaseworkerIssueApplicationIT {

    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP =
        "classpath:caseworker-issue-application-about-to-submit-app2-sol-rep-response.json";
    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_SOLICITOR_SERVICE =
        "classpath:caseworker-issue-application-about-to-submit-solicitor-service-response.json";
    private static final String ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP =
        "classpath:caseworker-issue-application-about-to-submit-app2-not-sol-rep-response.json";
    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR =
        "classpath:caseworker-issue-application-about-to-submit-error-response.json";
    private static final String SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-sole-citizen-application-about-to-submit-response.json";
    private static final String JOINT_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-joint-citizen-application-about-to-submit-response.json";
    public static final String DIVORCE_APPLICATION_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    public static final String AOS_COVER_LETTER_ID = "c35b1868-e397-457a-aa67-ac1422bb8100";
    public static final String NOTICE_OF_PROCEEDING_ID = "c56b053e-4184-11ec-81d3-0242ac130003";

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
    private NotificationService notificationService;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
        DocManagementStoreWireMock.start();
        SendLetterWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
        DocManagementStoreWireMock.stopAndReset();
        SendLetterWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        when(clock.instant()).thenReturn(Instant.parse("2021-06-17T12:00:00.000Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationWhenRespondentIsUkBased() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().getHomeAddress().setCountry("UK");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application").thenReturn("Notice of proceeding");

        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Sole.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse(SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT)));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_APPLICANT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationWhenRespondentIsOverseasBased() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().getHomeAddress().setCountry("France");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application").thenReturn("Notice of proceeding");

        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Overseas_Sole.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant2HomeAddress.Country", "France");

        assertThatJson(response).isEqualTo(jsonDocument.json());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_APPLICANT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForJointCitizenApplication() throws Exception {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Joint.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
                content().json(expectedResponse(JOINT_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT))
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(JOINT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(JOINT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateAosPackAndPostAosPackAndSendEmailsWhenRespondentIsRepresentedAndCourtService() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor
                .builder()
                .name("testsol")
                .email(TEST_SOLICITOR_EMAIL)
                .address("223b\nBaker Street\nLondon\nGreater London\nNW1 5FG\nUnited Kingdom")
                .organisationPolicy(organisationPolicy())
                .build()
        );
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Mini_Application_Sole_Joint.docx");
        stubAosPackSendLetter();
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));
        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyDivorceApplicationAndSetIssueDateAndSendEmailWhenRespondentIsNotSolicitorRepresented() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Mini_Application_Sole_Joint.docx");
        stubAosPackSendLetter();
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyDivorceApplicationAndSetIssueDateAndSendEmailWhenSolicitorServiceMethod() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setHomeAddress(null);
        caseData.getApplicant2().setSolicitor(
            Solicitor
                .builder()
                .name("testsol")
                .email(TEST_SOLICITOR_EMAIL)
                .address("223b\nBaker Street\nLondon\nGreater London\nNW1 5FG\nUnited Kingdom")
                .organisationPolicy(organisationPolicy())
                .build()
        );

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubAosPackSendLetter();
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_SOLICITOR_SERVICE)));

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_SERVICE),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void givenInvalidCaseDataWhenAboutToSubmitCallbackIsInvokedThenResponseContainsErrors() throws Exception {
        final CaseData caseData = invalidCaseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Divorce application");

        stubForDocAssembly();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

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
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized()
            );
    }

    @Test
    void shouldRemoveDraftApplicationDocumentIfCaseDataContainsDraftApplicationDocument() throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());

        final var documentUuid = setupAuthorizationAndApplicationDocument(caseData);
        stubDeleteFromDocumentManagementForSystem(documentUuid, OK);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubAosPackSendLetter();
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldRespondWithForbiddenStatusIfCaseDataHasDraftApplicationAndServiceNotWhiteListedWithDocStore()
        throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());

        final var documentUuid = setupAuthorizationAndApplicationDocument(caseData);
        stubDeleteFromDocumentManagementForSystem(documentUuid, FORBIDDEN);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubAosPackSendLetter();
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isForbidden());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRespondWithUnauthorizedStatusIfCaseDataHasDraftApplicationAndServiceAuthorizationFails() throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());

        final var documentUuid = setupAuthorizationAndApplicationDocument(caseData);
        stubDeleteFromDocumentManagementForSystem(documentUuid, UNAUTHORIZED);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubAosPackSendLetter();
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isUnauthorized());

        verifyNoInteractions(notificationService);
    }

    private AddressGlobalUK correspondenceAddress() {
        return AddressGlobalUK.builder()
            .addressLine1("223b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();
    }

    private void stubAosPackSendLetter() throws IOException {

        final var documentListValue1 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_ID);
        final var documentListValue2 = documentWithType(
            RESPONDENT_INVITATION,
            AOS_COVER_LETTER_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()));

        final byte[] pdfAsBytes = loadPdfAsBytes();

        for (String documentId : documentIds) {
            stubDownloadBinaryFromDocumentManagement(documentId, pdfAsBytes);
        }

        final SendLetterResponse sendLetterResponse = new SendLetterResponse(UUID.randomUUID());
        final ZonedDateTime now = ZonedDateTime.now();
        final LetterStatus letterStatus = new LetterStatus(
            sendLetterResponse.letterId,
            "OK",
            "",
            now,
            now,
            now,
            null,
            1);

        stubSendLetters(objectMapper.writeValueAsString(sendLetterResponse));
        stubStatusOfSendLetter(sendLetterResponse.letterId, objectMapper.writeValueAsString(letterStatus));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    private byte[] loadPdfAsBytes() throws IOException {
        return resourceAsBytes("classpath:Test.pdf");
    }

    private String setupAuthorizationAndApplicationDocument(CaseData caseData) {
        final var documentListValue = documentWithType(APPLICATION);
        final var generatedDocuments = singletonList(documentListValue);

        caseData.setDocumentsGenerated(generatedDocuments);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        return FilenameUtils.getName(documentListValue.getValue().getDocumentLink().getUrl());
    }
}
