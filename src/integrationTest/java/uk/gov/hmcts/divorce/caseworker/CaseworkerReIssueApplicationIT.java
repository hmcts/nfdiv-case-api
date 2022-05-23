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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReissueApplication.CASEWORKER_REISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;
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
        "classpath:caseworker-reissue-sole-citizen-application-about-to-submit-response.json";
    private static final String SOLE_CITIZEN_CASEWORKER_OFFLINE_AOS_ABOUT_TO_SUBMIT =
        "classpath:caseworker-reissue-sole-citizen-application-offlineAos-about-to-submit-response.json";
    private static final String JOINT_APPLICATION_APPLICANT1_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-reissue-joint-application-applicant1-solicitor-about-to-submit-response.json";
    private static final String JOINT_APPLICATION_APPLICANT2_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-reissue-joint-application-applicant2-solicitor-about-to-submit-response.json";
    private static final String SOLE_CITIZEN_CASEWORKER_PERSONAL_SERVICE_ABOUT_TO_SUBMIT =
        "classpath:caseworker-reissue-sole-citizen-application-personal-service-about-to-submit-response.json";
    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_SOLICITOR_SERVICE =
        "classpath:caseworker-issue-application-about-to-submit-solicitor-service-response.json";

    private static final String MINI_APPLICATION_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    private static final String AOS_COVER_LETTER_ID = "c35b1868-e397-457a-aa67-ac1422bb8100";
    private static final String NOTICE_OF_PROCEEDING_ID = "c56b053e-4184-11ec-81d3-0242ac130003";
    private static final String COVERSHEET_APPLICANT_ID = "af678800-4c5c-491c-9b7f-22056412ff94";
    private static final String CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID = "rf939456-4c5c-491c-9b7f-22056412ff94";
    private static final String CITIZEN_RESP_AOS_INVITATION_ONLINE_ID = "eb90a159-4200-410b-a504-5a925be0b152";
    private static final String DIVORCE_APPLICATION_TEMPLATE_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    private static final String NOTICE_OF_PROCEEDINGS_APP_2_ID = "8uh725e8-f053-8745-7gbt-bb69d1905ae3";
    private static final String NOTICE_OF_PROCEEDING_TEMPLATE_ID = "c56b053e-4184-11ec-81d3-0242ac130003";
    private static final String NOP_ONLINE_SOLE_RESP_TEMPLATE_ID = "2ecb05c1-6e3d-4508-9a7b-79a84e3d63aa";
    private static final String NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID = "eb780eb7-8982-40a7-b30f-902b582ded26";

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
    private DocumentUploadClientApi documentUploadClientApi;

    @MockBean
    private BulkPrintService bulkPrintService;

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
    void shouldSetReIssueDateAndSendApplicationIssueNotificationsForSoleCitizenApplicationDigitalAos() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Sole_V2.docx");
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

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSetReIssueDateAndSendApplicationIssueNotificationsForSoleCitizenApplicationOfflineAos() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Divorce application");


        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID, "NFD_Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse(SOLE_CITIZEN_CASEWORKER_OFFLINE_AOS_ABOUT_TO_SUBMIT)));

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSetReIssueDateAndSendApplicationIssueNotificationsForJointAppReissueCaseApplicant1Solicitor() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDueDate(LocalDate.now().plusDays(121));
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setReissueOption(REISSUE_CASE);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings app1")
            .thenReturn("Notice of proceedings app2")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Joint_V2.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .isEqualTo(json(expectedResponse(JOINT_APPLICATION_APPLICANT1_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT)));

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
    void shouldSetReIssueDateAndSendApplicationIssueNotificationsForJointAppReissueCaseApplicant2Solicitor() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setReissueOption(REISSUE_CASE);
        caseData.setDueDate(LocalDate.now().plusDays(121));
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().getSolicitor().setEmail(null);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder().organisationId("Org").build())
                    .build()
                )
                .build()
        );
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings app1")
            .thenReturn("Notice of proceedings app2")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Joint_V2.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Sole_Joint_Solicitor.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(JOINT_APPLICATION_APPLICANT2_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT)));

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
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
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent");

        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID, "NFD_Notice_Of_Proceedings_Sole_Respondent.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Sole_Joint_Solicitor.docx");
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
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
        caseData.getApplicant2().setEmail(null);
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
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Sole_Joint_Solicitor.docx");
        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID, "NFD_Notice_Of_Proceedings_Sole_Respondent_V2.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);

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

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_Notice_Of_Proceedings_Sole_Joint_Solicitor.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Sole_Respondent_V2.docx");
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            AOS_COVER_LETTER_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NOTICE_OF_PROCEEDING_TEMPLATE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            MINI_APPLICATION_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);

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
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
    void shouldGenerateOnlyRespAosAndInvitationAndSetReIssueDateAndSendEmailWhenRespIsNotSolicitorRepresentedAndReissueTypeIsDigitalAos()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Sole_Joint_Solicitor.docx");
        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_ONLINE_ID, "NFD_Notice_Of_Proceedings_Online_Respondent_Sole_V4.docx");
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
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(
                expectedResponse(REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP))
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateAndSendEmailWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsOfflineAos()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Overseas_Sole_V2.docx");
        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID, "NFD_Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final var documentListValue4 = documentWithType(
            COVERSHEET,
            COVERSHEET_APPLICANT_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue4.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);
        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
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
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(
                expectedResponse(REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_OFFLINE_AOS))
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateAndSendEmailWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsReissueCase()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(REISSUE_CASE);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Online_Respondent_Sole_V4.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Online_Respondent_Sole_V4.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetter();

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn().getResponse().getContentAsString();

        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(
                    expectedResponse(
                        REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_REISSUE_CASE_TYPE)
                )
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
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
    void givenInvalidCaseDataWhenAboutToSubmitCallbackIsInvokedThenResponseContainsErrors() throws Exception {
        final CaseData caseData = invalidCaseData();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
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

    @Test
    void shouldGenerateD10DocumentWhenSolicitorMethodIsSelected() throws Exception { // pass
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);
        caseData.setDueDate(LocalDate.now().plusDays(121));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Joint_V2.docx");

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
                status().isOk()
            )
            .andReturn().getResponse().getContentAsString();

        verify(documentUploadClientApi).upload(
            eq(TEST_AUTHORIZATION_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(CASEWORKER_USER_ID),
            anyList()
        );
    }

    @Test
    void shouldNotGenerateD10DocumentWhenD10HasAlreadyBeenGeneratedForCase() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDueDate(LocalDate.now().plusDays(121));
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);

        final ListValue<DivorceDocument> d10Document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D10)
                .documentFileName("D10.pdf")
                .build())
            .build();
        caseData.getDocuments().setDocumentsGenerated(singletonList(d10Document));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Joint_V2.docx");

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
            .andReturn()
            .getResponse()
            .getContentAsString();

        verifyNoInteractions(documentUploadClientApi);
    }

    @Test
    void shouldSendApplicationReissueNotificationsForSoleCitizenApplicationWhenRespondentOnlineAndPersonalService() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setCaseInvite(null);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Overseas_Sole_V2.docx");
        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID, "NFD_Notice_Of_Proceedings_Online_Respondent_Sole_V4.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final var documentListValue4 = documentWithType(
            COVERSHEET,
            COVERSHEET_APPLICANT_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue4.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);

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
        assertThatJson(response).isEqualTo(expectedPersonalServiceResponseRespondentOnline().json());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(OVERSEAS_RESPONDENT_HAS_EMAIL_APPLICATION_ISSUED),
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
    void shouldSendApplicationReissueNotificationsForSoleCitizenApplicationWhenRespondentOfflineAndPersonalService() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(null);
        caseData.setCaseInvite(null);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "NFD_Notice_Of_Proceedings_Overseas_Sole_V2.docx");
        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID, "NFD_Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final var documentListValue4 = documentWithType(
            COVERSHEET,
            COVERSHEET_APPLICANT_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue4.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);

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
        assertThatJson(response).isEqualTo(expectedPersonalServiceResponseRespondentOffline().json());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(OVERSEAS_RESPONDENT_NO_EMAIL_APPLICATION_ISSUED),
                anyMap(),
                eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyDivorceApplicationAndSetIssueDateAndSendEmailWhenSolicitorServiceMethod() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setApplicant1KnowsApplicant2Address(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setAddress(null);
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
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_Notice_Of_Proceedings_Sole_Applicant_Solicitor_Registered_V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID, "NFD_Notice_Of_Proceedings_Sole_Respondent_V2.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant2_Solicitor_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            AOS_COVER_LETTER_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final var documentListValue4 = documentWithType(
            COVERSHEET,
            COVERSHEET_APPLICANT_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue4.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);

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
    void shouldSendLettersOnReissueToOnlyApplicant1IfCourtServiceNotSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

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
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(bulkPrintService).printWithD10Form(any());
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldSendLettersOnReissueToBothApplicantsIfCourtServiceSelectedAndD10ToApplicant2() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(null);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Sole_Joint_Solicitor.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

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
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(bulkPrintService).print(any());
        verify(bulkPrintService).printAosRespondentPack(any(), eq(true));
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldNotSendAnyLettersOnReissueIfDigitalAosSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Online_Respondent_Sole_V4.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

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
            .andReturn()
            .getResponse()
            .getContentAsString();

        verifyNoInteractions(bulkPrintService);
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
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            CITIZEN_RESP_AOS_INVITATION_OFFLINE_ID);

        final var documentListValue3 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue4 = documentWithType(
            COVERSHEET,
            COVERSHEET_APPLICANT_ID);

        final var documentListValue5 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NOTICE_OF_PROCEEDINGS_APP_2_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue4.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue5.getValue().getDocumentLink().getUrl())
        );
        stubAosPackSendLetter(documentIds);
    }

    private void stubAosPackSendLetter(List<String> documentIds) throws IOException {

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

    private DocumentContext expectedPersonalServiceResponseRespondentOnline() throws IOException {
        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(SOLE_CITIZEN_CASEWORKER_PERSONAL_SERVICE_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant2Address.Country", "UK");
        jsonDocument.delete("data.applicant2InviteEmailAddress");
        jsonDocument.delete("data.noticeOfProceedingsEmail");
        return jsonDocument;
    }

    private DocumentContext expectedPersonalServiceResponseRespondentOffline() throws IOException {
        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(SOLE_CITIZEN_CASEWORKER_PERSONAL_SERVICE_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant2Address.Country", "UK");
        jsonDocument.delete("data.applicant2Email");
        jsonDocument.delete("data.applicant2InviteEmailAddress");
        jsonDocument.delete("data.noticeOfProceedingsEmail");
        return jsonDocument;
    }
}
