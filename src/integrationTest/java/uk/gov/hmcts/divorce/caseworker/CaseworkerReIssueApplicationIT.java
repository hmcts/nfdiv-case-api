package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.divorce.divorcecase.model.ReissueOption;
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
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_INVITATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock.stubDownloadBinaryFromDocumentManagement;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubSendLetters;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubStatusOfSendLetter;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
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
public class CaseworkerReIssueApplicationIT {

    private static final String CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP =
        "classpath:caseworker-reissue-application-about-to-submit-app2-sol-rep-response.json";
    private static final String CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP_OFFLINE_AOS =
        "classpath:caseworker-reissue-application-about-to-submit-app2-sol-rep-offline-aos-response.json";
    private static final String CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP_REISSUE_CASE_TYPE =
        "classpath:caseworker-reissue-application-about-to-submit-app2-sol-rep-reissue-response.json";
    private static final String REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP =
        "classpath:caseworker-reissue-application-about-to-submit-app2-not-sol-rep-response.json";
    private static final String REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_OFFLINE_AOS =
        "classpath:caseworker-reissue-application-about-to-submit-app2-not-sol-rep-offline-aos-response.json";
    private static final String REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_REISSUE_CASE_TYPE =
        "classpath:caseworker-reissue-application-about-to-submit-app2-not-sol-rep-reissue-case-response.json";
    private static final String CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR =
        "classpath:caseworker-issue-application-about-to-submit-error-response.json";
    private static final String SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-sole-citizen-application-about-to-submit-response.json";
    private static final String JOINT_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-joint-citizen-application-about-to-submit-response.json";
    public static final String MINI_APPLICATION_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    public static final String AOS_COVER_LETTER_ID = "c35b1868-e397-457a-aa67-ac1422bb8100";

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

    @ParameterizedTest
    @MethodSource("reissueOptions")
    void shouldSetReIssueDateAndSendApplicationIssueNotificationsForSoleCitizenApplication(ReissueOption reissueOption) throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setReissueOption(reissueOption);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(MINI_APPLICATION_ID, "NFD_CP_Mini_Application_Sole_Joint.docx");
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
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT))
            );

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

    @ParameterizedTest
    @MethodSource("reissueOptions")
    void shouldSetReIssueDateAndSendApplicationIssueNotificationsForJointCitizenApplication(ReissueOption reissueOption) throws Exception {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setReissueOption(reissueOption);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(MINI_APPLICATION_ID, "NFD_CP_Mini_Application_Sole_Joint.docx");
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
                        CASEWORKER_REISSUE_APPLICATION)))
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
    void shouldGenerateRespondentInvitationAndSetReIssueDateAndSendEmailNotificationsWhenRespondentIsRepresentedAndReissueTypeIsDigitalAos()
        throws Exception {
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
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
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
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP))
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
    void shouldGenerateRespondentAosAndSetReIssueDateAndNotSendEmailNotificationsWhenRespondentIsRepresentedAndReissueTypeOfflineAos()
        throws Exception {
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
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP_OFFLINE_AOS))
            );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateRespondentAosAndSetReIssueDateAndNotSendEmailNotificationsWhenRespondentIsRepresentedAndTypeIsReissueCase()
        throws Exception {
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
        caseData.getApplication().setReissueOption(REISSUE_CASE);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP_REISSUE_CASE_TYPE))
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
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateAndSendEmailWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsDigitalAos()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
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
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP))
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateAndSendEmailWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsOfflineAos()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_OFFLINE_AOS))
            );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateAndSendEmailWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsReissueCase()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setCorrespondenceAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(REISSUE_CASE);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Respondent Invitation").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, "NFD_CP_Application_Sole.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_REISSUE_CASE_TYPE))
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR))
            );
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
            MINI_APPLICATION_ID);
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

    private static Stream<Arguments> reissueOptions() {
        return Stream.of(
            Arguments.of(DIGITAL_AOS),
            Arguments.of(OFFLINE_AOS),
            Arguments.of(REISSUE_CASE)
        );
    }

}
