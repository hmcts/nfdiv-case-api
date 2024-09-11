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
import uk.gov.hmcts.divorce.divorcecase.model.JudicialSeparationReissueOption;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.CdamWireMock;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
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
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.DIGITAL_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.OFFLINE_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ReissueOption.REISSUE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OVERSEAS_RESPONDENT_APPLICATION_ISSUED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamDownloadBinaryWith;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamUploadWith;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssembly;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
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
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;
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
    private static final String REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_REISSUE_CASE_TYPE_WELSH =
        "classpath:caseworker-reissue-application-about-to-submit-app2-not-sol-rep-reissue-case-welsh-response.json";
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
    private static final String JOINT_APPLICATION_CASEWORKER_REISSUE_APPLICATION_JS_ABOUT_TO_SUBMIT =
        "classpath:caseworker-reissue-application-joint-js-about-to-submit-respondent-response.json";
    private static final String SOLE_JS_APPLICANT1_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT =
        "classpath:caseworker-issue-js-applicant1-represented-by-solicitor-about-to-submit-response.json";

    private static final String MINI_APPLICATION_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    private static final String AOS_COVER_LETTER_ID = "c35b1868-e397-457a-aa67-ac1422bb8100";
    private static final String NOTICE_OF_PROCEEDING_ID = "c56b053e-4184-11ec-81d3-0242ac130003";
    private static final String COVERSHEET_APPLICANT_ID = "af678800-4c5c-491c-9b7f-22056412ff94";
    private static final String CITIZEN_RESP_AOS_INVITATION_ONLINE_ID = "eb90a159-4200-410b-a504-5a925be0b152";
    private static final String DIVORCE_APPLICATION_TEMPLATE_ID = "5cd725e8-f053-4493-9cbe-bb69d1905ae3";
    private static final String NOTICE_OF_PROCEEDINGS_APP_2_ID = "8uh725e8-f053-8745-7gbt-bb69d1905ae3";
    private static final String NOTICE_OF_PROCEEDING_TEMPLATE_ID = "c56b053e-4184-11ec-81d3-0242ac130003";
    private static final String NOP_ONLINE_SOLE_RESP_TEMPLATE_ID = "2ecb05c1-6e3d-4508-9a7b-79a84e3d63aa";
    private static final String NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE_ID = "eb780eb7-8982-40a7-b30f-902b582ded26";
    private static final String NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID = "rf939456-4c5c-491c-9b7f-22056412ff94";
    private static final String NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE_ID = "2ecb05c1-6e3d-4508-9a7b-79a84e3d63aa";
    private static final String D84_DOCUMENT_ID = "67db1868-e917-457a-b9a7-209d1905810a";
    private static final String NFD_NOP_APP2_JS_SOLE_ID = "c35b1868-e397-457a-aa67-ac1422bb810a";
    private static final String D10_DOCUMENT_ID = "c35b1868-e397-457a-bb67-ac1422bb811a";
    private static final String NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_SOLE_TEMPLATE_ID = "68fd5d22-be58-11ed-afa1-0242ac120002";
    private static final String NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_JOINT_TEMPLATE_ID = "ac0b7b52-e8db-44ef-aad6-e050ebed89f6";

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
    void shouldSetReIssueDateAndGenerateDocumentsForSoleCitizenApplicationDigitalAos() throws Exception {
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
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-A1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
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

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldResetSpecificAosFieldsUponReissue() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getAcknowledgementOfService().setAosIsDrafted(YES);
        caseData.getAcknowledgementOfService().setConfirmReadPetition(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-A1-V3.docx");
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
            .andExpect(
                content().json(expectedResponse(SOLE_CITIZEN_CASEWORKER_ABOUT_TO_SUBMIT))
            ).andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.data.confirmReadPetition")
            .isAbsent();

        assertThatJson(response)
            .inPath("$.data.aosIsDrafted")
            .isAbsent();
    }

    @Test
    void shouldNotResetSpecificAosFieldsUponReissueIfJoint() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getAcknowledgementOfService().setAosIsDrafted(YES);
        caseData.getAcknowledgementOfService().setConfirmReadPetition(YES);
        caseData.setDueDate(LocalDate.now().plusDays(121));
        caseData.setApplicationType(JOINT_APPLICATION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceedings app1")
            .thenReturn("Notice of proceedings app2")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

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
            .inPath("$.data.confirmReadPetition")
            .isEqualTo(YES);

        assertThatJson(response)
            .inPath("$.data.aosIsDrafted")
            .isEqualTo(YES);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationDigitalAos() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
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
    void shouldSetReIssueDateAndGenerateDocumentsForSoleCitizenApplicationOfflineAos() throws Exception {
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

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R2-V11.docx");
        stubForDocAssemblyWith(NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_ReIssue_Offline_V1.docx");
        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

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
            .isEqualTo(json(expectedResponse(SOLE_CITIZEN_CASEWORKER_OFFLINE_AOS_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForSoleCitizenApplicationOfflineAos() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setReissueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now());
        caseData.getApplicant2().setOffline(YES);

        stubAosPackSendLetter();
        stubCdamUploadWith(D10_DOCUMENT_ID, D10.getLabel());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldSetReIssueDateAndGenerateDocumentsForJointAppReissueCaseApplicant1Solicitor() throws Exception {
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
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

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
            .isEqualTo(json(expectedResponse(JOINT_APPLICATION_APPLICANT1_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForJointAppReissueCaseApplicant1Solicitor() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDueDate(LocalDate.now().plusDays(121));
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        caseData.getApplication().setPreviousReissueOption(REISSUE_CASE);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setApplicant1KnowsApplicant2EmailAddress(YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
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
    void shouldSetReIssueDateAndGenerateDocumentsForJointAppReissueCaseApplicant2Solicitor() throws Exception {
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
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");
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

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendApplicationIssueNotificationsForJointAppReissueCaseApplicant2Solicitor() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setPreviousReissueOption(REISSUE_CASE);
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

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
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
    void shouldGenerateRespondentInvitationAndSetReIssueDateWhenRespondentIsRepresentedAndReissueTypeIsDigitalAos()
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

        stubForDocAssemblyWith(NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Sole-Respondent-V3.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
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

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationsWhenRespondentIsRepresentedAndReissueTypeIsDigitalAos()
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
        caseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);

        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setReissueDate(issueDate.plusDays(2));
        caseData.setDueDate(issueDate.plusDays(14));

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE),
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
    void shouldGenerateRespondentAosAndSetReIssueDateWhenRespondentIsRepresentedAndReissueTypeOfflineAos()
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

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Sole-Respondent-V3.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);

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
                content().json(expectedResponse(CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP_OFFLINE_AOS))
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendEmailNotificationsWhenRespondentIsRepresentedAndReissueTypeOfflineAos()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setEmail(null);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitor(
            Solicitor
                .builder()
                .name("testsol")
                .email(TEST_SOLICITOR_EMAIL)
                .address("223b\nBaker Street\nLondon\nGreater London\nNW1 5FG\nUnited Kingdom")
                .organisationPolicy(organisationPolicy())
                .build()
        );
        caseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);
        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setReissueDate(issueDate.plusDays(2));
        caseData.setDueDate(issueDate.plusDays(14));

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID);

        final var documentListValue3 = documentWithType(
            APPLICATION,
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final List<String> documentIds = asList(
            FilenameUtils.getName(documentListValue1.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue2.getValue().getDocumentLink().getUrl()),
            FilenameUtils.getName(documentListValue3.getValue().getDocumentLink().getUrl())
        );

        stubAosPackSendLetter(documentIds);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldGenerateRespondentAosAndSetReIssueDateWhenRespondentIsRepresentedAndTypeIsReissueCase()
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

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(MINI_APPLICATION_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
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
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(CASEWORKER_REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_SOL_REP_REISSUE_CASE_TYPE))
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailNotificationsWhenRespondentIsRepresentedAndTypeIsReissueCase()
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
        caseData.getApplication().setPreviousReissueOption(REISSUE_CASE);
        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setReissueDate(issueDate.plusDays(2));
        caseData.setDueDate(issueDate.plusDays(14));

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

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE),
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
    void shouldGenerateOnlyRespAosAndInvitationAndSetReIssueDateWhenRespIsNotSolicitorRepresentedAndReissueTypeIsDigitalAos()
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

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(CITIZEN_RESP_AOS_INVITATION_ONLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
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

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendEmailWhenRespIsNotSolicitorRepresentedAndReissueTypeIsDigitalAos()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(correspondenceAddress());
        caseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);
        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setReissueDate(issueDate.plusDays(2));
        caseData.setDueDate(issueDate.plusDays(14));

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE),
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
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsOfflineAos()
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

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AL2-V3.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R2-V11.docx");
        stubForDocAssemblyWith(NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_REISSUE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_ReIssue_Offline_V1.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");

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
                expectedResponse(REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_OFFLINE_AOS))
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyRespondentAosAndSetReIssueDateWhenRespondentIsNotSolicitorRepresentedAndReissueTypeIsReissueCase()
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

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-R1-V10.docx");

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

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldGenerateOnlyRespAosAndSetReIssueDateWhenRespIsNotRepresentedAndReissueTypeIsReissueCaseAndLangPrefIsWelsh()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(correspondenceAddress());
        caseData.getApplication().setReissueOption(REISSUE_CASE);
        caseData.getApplication().setIssueDate(LocalDate.now());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceedings respondent")
            .thenReturn("Divorce application");

        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-WEL-Notice_Of_Proceedings_Online_Respondent_Sole_V7.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_SOLE_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS1-V3.docx");

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
                status().isOk()
            )
            .andReturn().getResponse().getContentAsString();

        assertThatJson(response)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(
                    expectedResponse(
                        REISSUE_APPLICATION_ABOUT_TO_SUBMIT_APP_2_NOT_SOL_REP_REISSUE_CASE_TYPE_WELSH)
                )
            );

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendWelshEmailWhenRespIsNotRepresentedAndReissueTypeIsReissueCaseAndLangPrefIsWelsh()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setAddress(correspondenceAddress());
        caseData.getApplication().setPreviousReissueOption(REISSUE_CASE);
        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setReissueDate(issueDate.plusDays(2));
        caseData.setDueDate(issueDate.plusDays(14));

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
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
            );

        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLE_APPLICANT_SOLICITOR_NOTICE_OF_PROCEEDINGS_REISSUE),
                anyMap(),
                eq(ENGLISH),
                anyLong());

        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED),
                argThat(allOf(
                    hasEntry(CommonContent.PARTNER, "gwraig"))
                ),
                eq(WELSH),
                anyLong());

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
    void shouldGenerateD10DocumentWhenSoleApplicationAndSolicitorMethodIsSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
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
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");
        stubCdamUploadWith(D10_DOCUMENT_ID, D10.getLabel());
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

    }

    @Test
    void shouldNotGenerateD10DocumentWhenD10HasAlreadyBeenGeneratedForCase() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(SOLE_APPLICATION);
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
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

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

    }

    @Test
    void shouldNotGenerateD10DocumentWhenJointApplication() throws Exception {
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

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId()).thenReturn("Notice of proceedings respondent").thenReturn("Divorce application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_CP_Dummy_Template.docx");
        stubForDocAssemblyWith(DIVORCE_APPLICATION_TEMPLATE_ID, TEST_DIVORCE_APPLICATION_JOINT_TEMPLATE_ID);
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-V3.docx");

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
        caseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID);

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

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION,
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
        caseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID);

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

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION,
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
    void shouldSendApplicationReissueNotificationInWelshForSoleCitizenApplicationWhenRespondentOnlineAndPersonalService()
        throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.setDueDate(LocalDate.of(2021, 6, 20));
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setDivorceWho(WIFE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.setCaseInvite(null);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        final var documentListValue1 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_1,
            NOTICE_OF_PROCEEDING_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID);

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
    void shouldSendSolicitorServiceEmailWhenSolicitorServiceMethod() throws Exception {
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
        caseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);

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

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
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
    void shouldSendLettersOnReissueToOnlyApplicant1IfCourtServiceNotSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(14));
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        ListValue<DivorceDocument> divorceDocumentListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(divorceDocumentListValue))
            .build());

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
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
    void shouldGenerateR2WelshNoticeOfProceedingsForRespondentIfRespondentLanguagePreferenceIsWelsh() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setReissueOption(OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-AS2-V6.docx");
        stubForDocAssemblyWith(NOP_ONLINE_SOLE_RESP_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_ReIssue_Offline_V1.docx");
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
            .andReturn();
    }

    @Test
    void shouldSendLettersOnReissueToBothApplicantsIfCourtServiceSelectedAndD10ToApplicant2() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setPreviousReissueOption(OFFLINE_AOS);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(14));
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(NO);
        caseData.getApplicant2().setEmail(null);

        ListValue<DivorceDocument> divorceDocumentListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        caseData.setDocuments(CaseDocuments.builder()
            .documentsGenerated(Lists.newArrayList(divorceDocumentListValue))
            .build());

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verify(bulkPrintService).print(any());
        verify(bulkPrintService).printAosRespondentPack(any(), eq(true));
        verifyNoMoreInteractions(bulkPrintService);
    }

    @Test
    void shouldNotSendAnyLettersOnReissueIfDigitalAosSelected() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplication().setPreviousReissueOption(DIGITAL_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(14));
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        stubAosPackSendLetter();

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(
                        caseData,
                        CASEWORKER_REISSUE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk());

        verifyNoInteractions(bulkPrintService);
    }

    @Test
    void shouldSendLettersToBothApplicantsIfJointJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setJudicialSeparationReissueOption(JudicialSeparationReissueOption.OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.setDueDate(LocalDate.now().plusDays(121));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant 1")
            .thenReturn("Coversheet applicant 2")
            .thenReturn("Notice of proceeding applicant 2")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_TEMPLATE_ID, "FL-NFD-GOR-ENG-Notice-Of-Proceedings-Joint-JS.docx");
        stubForDocAssemblyWith(COVERSHEET_APPLICANT_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_JOINT_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Joint.docx");

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubCdamUploadWith(D84_DOCUMENT_ID, D84.getLabel());

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
                    JOINT_APPLICATION_CASEWORKER_REISSUE_APPLICATION_JS_ABOUT_TO_SUBMIT
                )
            ));

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSetReIssueDateAndGenerateDocumentsForSoleJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setSolSignStatementOfTruth(null);
        caseData.getApplication().setJudicialSeparationReissueOption(JudicialSeparationReissueOption.OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.now());
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant1().setEmail(null);
        caseData.getApplicant2().setEmail(null);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_SOLE_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Sole.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Applicant_JS_Sole.docx");
        stubForDocAssemblyWith(NFD_NOP_APP2_JS_SOLE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_JS_Sole.docx");
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
                status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSetReIssueDateAndGenerateDocumentsForSoleApplicantSolicitorJudicialSeparation() throws Exception {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getApplication().setJudicialSeparationReissueOption(JudicialSeparationReissueOption.OFFLINE_AOS);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 17));
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("Sol1")
            .reference("1234")
            .build());
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant1().setEmail(null);
        caseData.getApplicant2().setEmail(null);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(documentIdProvider.documentId())
            .thenReturn("Notice of proceeding applicant solicitor")
            .thenReturn("Notice of proceeding respondent")
            .thenReturn("Coversheet")
            .thenReturn("Judicial separation application");

        stubForDocAssemblyWith(AOS_COVER_LETTER_ID, "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith(NFD_NOP_JUDICIAL_SEPARATION_APPLICATION_SOLE_TEMPLATE_ID,
            "FL-NFD-APP-ENG-Judicial-Separation-Application-Sole.docx");
        stubForDocAssemblyWith(NOTICE_OF_PROCEEDING_ID, "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Applicant_Solicitor_JS_Sole.docx");
        stubForDocAssemblyWith(NFD_NOP_APP2_JS_SOLE_ID,
            "FL-NFD-GOR-ENG-Notice_Of_Proceedings_Respondent_JS_Sole.docx");
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
            .andReturn().getResponse().getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(
            expectedResponse(SOLE_JS_APPLICANT1_REPRESENTED_BY_SOLICITOR_CASEWORKER_ABOUT_TO_SUBMIT));
        jsonDocument.put("data", "previousReissueOption", "offlineAos");
        jsonDocument.put("data", "reissueDate", "2021-06-17");

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(jsonDocument.json());

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
            DIVORCE_APPLICATION_TEMPLATE_ID);

        final var documentListValue2 = documentWithType(
            NOTICE_OF_PROCEEDINGS_APP_2,
            NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE_ID);

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
}
