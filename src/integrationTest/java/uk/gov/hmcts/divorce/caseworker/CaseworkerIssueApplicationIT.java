package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.util.Lists;
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
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.testutil.CdamWireMock;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.divorce.testutil.TestResourceUtil;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.EndCivilPartnership.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamDeleteWith;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamDownloadBinaryWith;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamUploadWith;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyUnauthorized;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
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
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    CdamWireMock.PropertiesInitializer.class,
    SendLetterWireMock.PropertiesInitializer.class})
public class CaseworkerIssueApplicationIT {

    private static final String DIVORCE_APPLICATION_TEMPLATE_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    private static final String AOS_COVER_LETTER_TEMPLATE_ID = "c35b1868-e397-457a-aa67-ac1422bb8100";
    private static final String SOLICITOR_COVERSHEET_ID = "b66b90e4-156c-4090-b788-365219f0118b";
    private static final String NOTICE_OF_PROCEEDING_TEMPLATE_ID = "c56b053e-4184-11ec-81d3-0242ac130003";
    private static final String NOP_ONLINE_SOLE_RESP_TEMPLATE_ID = "2ecb05c1-6e3d-4508-9a7b-79a84e3d63aa";
    private static final String APPLICANT_COVERSHEET_TEMPLATE_ID = "af678800-4c5c-491c-9b7f-22056412ff94";
    private static final String NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID = "eb780eb7-8982-40a7-b30f-902b582ded26";
    private static final String D84_DOCUMENT_ID = "67db1868-e917-457a-b9a7-209d1905810a";
    private static final String NFD_NOP_APP2_JS_SOLE_ID = "c35b1868-e397-457a-aa67-ac1422bb810a";
    private static final String NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_SOLE_TEMPLATE_ID = "68fd5d22-be58-11ed-afa1-0242ac120002";
    private static final String NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_JOINT_TEMPLATE_ID = "ac0b7b52-e8db-44ef-aad6-e050ebed89f6";

    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_SOLICITOR_SERVICE =
        "classpath:caseworker-issue-application-about-to-submit-solicitor-service-response.json";
    private static final String ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP =
        "classpath:caseworker-issue-application-about-to-submit-app2-not-sol-rep-response.json";
    private static final String CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR =
        "classpath:caseworker-issue-application-about-to-submit-error-response.json";
    private static final String SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-sole-citizen-application-about-to-submit-response.json";
    private static final String SOLE_CITIZEN_UK_BASED_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-sole-citizen-uk-based-application-about-to-submit-response.json";
    private static final String SOLE_APPLICANT1_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-applicant1-represented-by-solicitor-about-to-submit-response.json";
    private static final String JOINT_APPLICATION_APP1_REPRESENTED_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-joint-application-app1-represented-about-to-submit-response.json";
    private static final String JOINT_APPLICATION_APP2_REPRESENTED_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-joint-application-app2-represented-about-to-submit-response.json";
    private static final String APP2_CONTACT_PRIVATE_CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-joint-application-app2-contact-private-about-to-submit-response.json";
    private static final String JOINT_APPLICATION_IN_WELSH_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-joint-application-in-welsh-about-to-submit-response.json";
    private static final String JOINT_APPLICATION_CASEWORKER_ISSUE_APPLICATION_JS_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-application-joint-js-about-to-submit-respondent-response.json";
    private static final String SOLE_JS_APPLICANT1_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-js-applicant1-represented-by-solicitor-about-to-submit-response.json";
    private static final String JOINT_JS_APPLICANTS_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-js-applicant1-applicant2-represented-by-solicitor-joint-about-to-submit-response.json";

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
    private CcdUpdateService ccdUpdateService;

    @MockBean
    private BulkPrintService bulkPrintService;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
        CdamWireMock.start();
        SendLetterWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
        CdamWireMock.stopAndReset();
        SendLetterWireMock.stopAndReset();
    }

    @BeforeEach
    void setClock() {
        when(clock.instant()).thenReturn(Instant.parse("2021-06-17T12:00:00.000Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForSoleCitizenApplicationWhenRespondentIsUkBased() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("UK");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-A1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

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
            .isEqualTo(json(TestResourceUtil.expectedResponse(SOLE_CITIZEN_UK_BASED_CASEWORKER_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationWhenRespondentIsUkBased() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("UK");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_APPLICANT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForSoleCitizenApplicationWhenRespondentIsUkBasedAndLanguagePreferenceIsWelsh()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("UK");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-A1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-WEL-Notice_Of_Proceedings_Online_Respondent_Sole_V7.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

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

        DocumentContext jsonDocument = JsonPath.parse(TestResourceUtil.expectedResponse(SOLE_CITIZEN_UK_BASED_CASEWORKER_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant2LanguagePreferenceWelsh", "Yes");

        assertThatJson(response)
            .isEqualTo(jsonDocument.json());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationWhenRespondentIsUkBasedAndLanguagePreferenceIsWelsh()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("UK");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_APPLICANT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                argThat(allOf(
                    hasEntry(CommonContent.PARTNER, "gwraig"))
                ),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateWelshNoPForApplicant1ForSoleCitizenApplicationWhenLanguagePreferenceIsWelsh()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("UK");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Sole_V2_CY.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-WEL-Notice_Of_Proceedings_Online_Respondent_Sole_V7.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, "NFD_CP_Application_Sole_Cy.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

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

        DocumentContext jsonDocument = JsonPath.parse(TestResourceUtil.expectedResponse(SOLE_CITIZEN_UK_BASED_CASEWORKER_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant1LanguagePreferenceWelsh", "Yes");

        assertThatJson(response)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForApplicant1SolicitorWhenIsRepresentedInDissolutionCase() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplicant1().getApplicantPrayer().setPrayerEndCivilPartnership(Set.of(END_CIVIL_PARTNERSHIP));
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant1().getSolicitor().setReference("TEST");
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

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
            .isEqualTo(json(TestResourceUtil.expectedResponse(SOLE_APPLICANT1_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForApplicant1SolicitorWhenIsRepresentedInDissolutionCase() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplicant1().getApplicantPrayer().setPrayerEndCivilPartnership(Set.of(END_CIVIL_PARTNERSHIP));
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant1().getSolicitor().setReference("TEST");
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendWelshApplicationIssueNotificationsForApplicant1SolicitorWhenLanguagePreferenceIsWelsh() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplicant1().getApplicantPrayer().setPrayerEndCivilPartnership(Set.of(END_CIVIL_PARTNERSHIP));
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant1().getSolicitor().setReference("TEST");
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant1().setGender(FEMALE);
        caseData.getApplicant2().setGender(FEMALE);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(WELSH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForSoleCitizenApplicationWhenRespondentIsOverseasBased() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("France");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AL2-V3.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Outside-England-Wales-R2-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

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

        assertThatJson(response).isEqualTo(expectedResponse().json());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationToApplicant1ForSoleCitizenApplicationWhenRespondentIsOverseasBased() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("France");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION,
                        "AwaitingService")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(OVERSEAS_RESPONDENT_APPLICATION_ISSUED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationInWelshToApplicant1ForSoleCitizenApplicationWhenRespondentIsOverseasBased()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().getAddress().setCountry("France");
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION, "AwaitingService")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(OVERSEAS_RESPONDENT_APPLICATION_ISSUED),
                anyMap(),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldIssueApplicationAndgenerateDocumentsForSoleCitizenApplicationWhenPersonalService() throws Exception {
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

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AL2-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R2-V11.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

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

        assertThatJson(response).isEqualTo(expectedPersonalServiceResponse().json());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationWhenPersonalService() throws Exception {
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

        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION,
                        "AwaitingService")))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(OVERSEAS_RESPONDENT_APPLICATION_ISSUED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateWelshR2NoticeOfProceedingsForRespondentIfRespondentLanguagePreferenceIsWelsh() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(null);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.setCaseInvite(null);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AL2-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-WEL-Notice_Of_Proceedings_Paper_Respondent_V8.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

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
            .andReturn();
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForJointApplication() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

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
            .isEqualTo(json(TestResourceUtil.expectedResponse(JOINT_APPLICATION_APP1_REPRESENTED_CASEWORKER_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsToApplicant1SolicitorForJointApplication() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);

        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_APPLICANT_2_USER_EMAIL),
                eq(JOINT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsWhenApplicant2IsRepresentedInJointApplication() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().getSolicitor().setEmail(null);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder().organisationId("Org").build())
                    .build()
                )
                .build()
        );
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Notice of proceedings app2")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");

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
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(TestResourceUtil.expectedResponse(JOINT_APPLICATION_APP2_REPRESENTED_CASEWORKER_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsToApplicant2SolicitorForJointApplication() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().getSolicitor().setEmail(null);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder().organisationId("Org").build())
                    .build()
                )
                .build()
        );
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(JOINT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateWelshJointNoticeOfProceedingsForApplicant1IfLanguagePreferenceChosenIsWelsh() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().getSolicitor().setEmail(null);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).build());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                    .organisation(Organisation.builder().organisationId("Org").build())
                    .build()
                )
                .build()
        );
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Notice of proceedings app2")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Joint_V2_Cy.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
            .andReturn();
    }

    @Test
    void shouldGenerateAosPackWhenRespondentIsRepresentedAndCourtService() throws Exception {
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
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Sole-Respondent-V3.docx");
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
                status().isOk()
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendAosPackAndEmailsWhenRespondentIsRepresentedAndCourtService() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
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
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH),
                anyLong());
        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyDivorceApplicationAndSetIssueDateWhenRespondentIsNotSolicitorRepresented() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(applicantAddress());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-A1-V3.docx");
        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, "NFD_CP_Mini_Application_Sole_Joint.docx");

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
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(TestResourceUtil.expectedResponse(ISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailWhenRespondentIsNotSolicitorRepresented() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(applicantAddress());

        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, AOS_COVER_LETTER_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyDivorceApplicationAndSetIssueDateWhenSolicitorServiceMethod() throws Exception {
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

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS2-V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Sole-Respondent-V3.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "FL-NFD-GOR-ENG-Solicitor-Coversheet.docx");
        stubAosPackSendLetterToApplicant1NotCourtService(AOS_COVER_LETTER_TEMPLATE_ID, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID);
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
            .isEqualTo(json(TestResourceUtil.expectedResponse(CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_SOLICITOR_SERVICE)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendAosPackAndEmailWhenSolicitorServiceMethod() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
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

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS2-V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Sole-Respondent-V3.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "FL-NFD-GOR-ENG-Solicitor-Coversheet.docx");
        stubAosPackSendLetterToApplicant1NotCourtService(AOS_COVER_LETTER_TEMPLATE_ID, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_SERVICE),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSendEmailWithWelshContentWhenSolicitorServiceMethod() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setApplicant1KnowsApplicant2Address(YES);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
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

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS2-V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Sole-Respondent-V3.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "FL-NFD-GOR-ENG-Solicitor-Coversheet.docx");
        stubAosPackSendLetterToApplicant1NotCourtService(AOS_COVER_LETTER_TEMPLATE_ID, NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(APPLICANT_SOLICITOR_SERVICE),
                argThat(allOf(
                    hasEntry("union type", DIVORCE_WELSH)
                )),
                eq(WELSH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfStateIsAwaitingService() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplicant2().setSolicitorRepresented(YES);

        CallbackRequest callbackRequest = callbackRequest(
            caseData,
            CASEWORKER_ISSUE_APPLICATION);

        callbackRequest.getCaseDetails().setState(AwaitingService.name());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ccdUpdateService)
            .submitEvent(
                anyLong(),
                eq(SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK),
                any(),
                eq(TEST_SERVICE_AUTH_TOKEN));
    }

    @Test
    void shouldNotSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfStateIsHolding() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));

        CallbackRequest callbackRequest = callbackRequest(
            caseData,
            CASEWORKER_ISSUE_APPLICATION);

        callbackRequest.getCaseDetails().setState(Holding.name());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verifyNoMoreInteractions(ccdUpdateService);
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
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(TestResourceUtil.expectedResponse(CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT_ERROR))
            );
    }

    @Test
    void givenValidCaseDataWhenAuthorizationFailsForDocAssemblyThenStatusIsUnauthorized() throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(applicantAddress());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

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
    void shouldRespondWithForbiddenStatusIfCaseDataHasDraftApplicationAndServiceNotWhiteListedWithDocStore()
        throws Exception {

        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(applicantAddress());

        final var documentUuid = setupAuthorizationAndApplicationDocument(caseData);
        stubCdamDeleteWith(documentUuid, FORBIDDEN);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();
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
        caseData.getApplicant2().setAddress(applicantAddress());

        final var documentUuid = setupAuthorizationAndApplicationDocument(caseData);
        stubCdamDeleteWith(documentUuid, UNAUTHORIZED);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();
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

    @Test
    void shouldAddNOPDocumentToConfidentialDocumentsIfApplicant1ContactIsPrivate() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseData.getApplicant2().setSolicitorRepresented(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
            .isEqualTo(json(TestResourceUtil.expectedResponse(APP2_CONTACT_PRIVATE_CASEWORKER_ISSUE_APPLICATION_ABOUT_TO_SUBMIT)));
    }

    @Test
    void shouldGenerateD10DocumentWhenSoleApplicationAndSolicitorMethodIsSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Overseas_Sole_V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
                status().isOk()
            )
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    void shouldNotGenerateD10DocumentWhenCourtServiceMethodIsSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
            .andReturn()
            .getResponse()
            .getContentAsString();

    }

    @Test
    void shouldNotGenerateD10DocumentWhenD10HasAlreadyBeenGeneratedForCase() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
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
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Overseas_Sole_V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Paper_Respondent_V6.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1NotCourtService(AOS_COVER_LETTER_TEMPLATE_ID, NOTICE_OF_PROCEEDING_TEMPLATE_ID);

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
            .andReturn()
            .getResponse()
            .getContentAsString();

    }

    @Test
    void shouldNotGenerateD10DocumentWhenJointApplication() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
            .andReturn()
            .getResponse()
            .getContentAsString();

    }

    @Test
    void shouldSendLettersToOnlyApplicant1IfCourtServiceNotSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(7));
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        addGeneratedDocuments(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(bulkPrintService).printWithD10Form(any());
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldSendLettersToBothApplicantsIfCourtServiceSelectedAndD10ToApplicant2() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(null);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(14));
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(NO);
        addGeneratedDocuments(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R2-V11.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(bulkPrintService).print(any());
        verify(bulkPrintService).printAosRespondentPack(any(), eq(true));
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldSendLettersToBothApplicantsForJointApplicationIfCourtServiceSelectedToApplicant2() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail("notNull@email.com");
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(14));
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(NO);
        addGeneratedDocuments(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(bulkPrintService).print(any());
        verify(bulkPrintService).printAosRespondentPack(any(), eq(false));
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldSendLettersToBothApplicantsIfCourtServiceSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail("notNull@email.com");
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(14));
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(NO);
        addGeneratedDocuments(caseData);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_ISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(bulkPrintService).print(any());
        verify(bulkPrintService).printAosRespondentPack(any(), eq(false));
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldGenerateNoPDocsAndCoversheetForBothApplicantsIfJointJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant 1")
            .thenReturn("Coversheet applicant 2")
            .thenReturn("Notice of proceeding applicant 2")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-JS.docx");
        stubForDocAssemblyWith(APPLICANT_COVERSHEET_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_JOINT_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Joint.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubCdamUploadWith(D84_DOCUMENT_ID, D84.getLabel());
        stubAosPackSendLetterToApplicant1NotCourtService(NOTICE_OF_PROCEEDING_TEMPLATE_ID, NOTICE_OF_PROCEEDING_TEMPLATE_ID);
        stubAosPackSendLetterToApplicant2();

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
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(
                    TestResourceUtil.expectedResponse(
                        JOINT_APPLICATION_CASEWORKER_ISSUE_APPLICATION_JS_ABOUT_TO_SUBMIT)
                )
            );
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForJointApplicationInWelsh() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant 1")
            .thenReturn("Notice of proceedings applicant 2")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, "FL-NFD-APP-WEL-Divorce-Application-Joint.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "NFD_Notice_Of_Proceedings_Joint_V2_Cy.docx");

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
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(TestResourceUtil.expectedResponse(JOINT_APPLICATION_IN_WELSH_CASEWORKER_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForApplicantForSoleJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Applicant_JS_Sole.docx");
        stubForDocAssemblyWith(NFD_NOP_APP2_JS_SOLE_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_JS_Sole.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_SOLE_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Sole.docx");
        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
                status().isOk()
            )
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForApplicantSolicitorForSoleJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("Sol1")
            .reference("1234")
            .build());
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant solicitor")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Applicant_Solicitor_JS_Sole.docx");
        stubForDocAssemblyWith(NFD_NOP_APP2_JS_SOLE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_JS_Sole.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_SOLE_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Sole.docx");
        stubForDocAssemblyWith(AOS_COVER_LETTER_TEMPLATE_ID, "NFD_Applicant_Coversheet.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
                status().isOk()
            ).andReturn().getResponse().getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(TestResourceUtil.expectedResponse(SOLE_JS_APPLICANT1_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT)));

    }

    @Test
    void shouldIssueApplicationAndGenerateDocumentsForApplicant1SolicitorForJointJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("Sol1")
            .reference("1234")
            .build());
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("Sol2")
            .reference("4321")
            .build());
        caseData.getApplicant2().setOffline(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant solicitor")
            .thenReturn("Coversheet applicant2 solicitor")
            .thenReturn("Notice of proceeding applicant2 solicitor")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Applicant_Solicitor_JS_Joint.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_JOINT_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Joint.docx");
        stubForDocAssemblyWith(SOLICITOR_COVERSHEET_ID, "FL-NFD-GOR-ENG-Solicitor-Coversheet.docx");

        stubCdamUploadWith(D84_DOCUMENT_ID, D84.getLabel());
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubAosPackSendLetterToApplicant1CourtService(AOS_COVER_LETTER_TEMPLATE_ID);

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
                status().isOk()
            ).andReturn().getResponse().getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(TestResourceUtil.expectedResponse(JOINT_JS_APPLICANTS_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT)));
    }

    private AddressGlobalUK applicantAddress() {
        return AddressGlobalUK.builder()
            .addressLine1("223b")
            .addressLine2("Baker Street")
            .postTown("London")
            .county("Greater London")
            .postCode("NW1 6XE")
            .country("United Kingdom")
            .build();
    }

    private void stubAosPackSendLetterToApplicant1CourtService(String nopTemplateId) throws IOException {

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            nopTemplateId);
        final var documentListValue2 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);


        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl())
        );

        final byte[] pdfAsBytes = loadPdfAsBytes();

        for (String documentId : documentIds) {
            stubCdamDownloadBinaryWith(documentId, pdfAsBytes);
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

    private void stubAosPackSendLetterToApplicant1NotCourtService(String nopApp1TemplateId,
                                                                  String nopApp2TemplateId) throws IOException {

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            nopApp1TemplateId);
        final var documentListValue2 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);
        final var documentListValue3 = documentWithType(
            COVERSHEET,
            APPLICANT_COVERSHEET_TEMPLATE_ID);
        final var documentListValue4 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            nopApp2TemplateId);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue4.getValue().getDocumentLink().getUrl())
        );

        final byte[] pdfAsBytes = loadPdfAsBytes();

        for (String documentId : documentIds) {
            stubCdamDownloadBinaryWith(documentId, pdfAsBytes);
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

    private void stubAosPackSendLetterToApplicant2() throws IOException {

        final var documentListValue1 = documentWithType(
            COVERSHEET,
            APPLICANT_COVERSHEET_TEMPLATE_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NOP_ONLINE_SOLE_RESP_TEMPLATE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl())
        );

        final byte[] pdfAsBytes = loadPdfAsBytes();

        for (String documentId : documentIds) {
            stubCdamDownloadBinaryWith(documentId, pdfAsBytes);
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

        caseData.getDocuments().setDocumentsGenerated(generatedDocuments);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        return FilenameUtils.getName(documentListValue.getValue().getDocumentLink().getUrl());
    }

    private DocumentContext expectedResponse() throws IOException {
        DocumentContext jsonDocument = JsonPath.parse(TestResourceUtil.expectedResponse(SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant2Address.Country", "France");
        jsonDocument.delete("data.noticeOfProceedingsEmail");
        return jsonDocument;
    }

    private DocumentContext expectedPersonalServiceResponse() throws IOException {
        DocumentContext jsonDocument = JsonPath.parse(TestResourceUtil.expectedResponse(SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT));
        jsonDocument.set("data.applicant2Address.Country", "UK");
        jsonDocument.delete("data.applicant2Email");
        jsonDocument.delete("data.applicant2InviteEmailAddress");
        jsonDocument.delete("data.noticeOfProceedingsEmail");
        return jsonDocument;
    }

    private void addGeneratedDocuments(final CaseData caseData) {
        ListValue<DivorceDocument> divorceDocumentListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(divorceDocumentListValue))
            .build());
    }
}
