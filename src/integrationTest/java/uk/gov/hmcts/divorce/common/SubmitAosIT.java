package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.ArrayUtils;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.testutil.CdamWireMock;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitAos.SUBMIT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueAosUnDisputed.SYSTEM_ISSUE_AOS_UNDISPUTED;
import static uk.gov.hmcts.divorce.testutil.CdamWireMock.stubCdamDownloadBinaryWith;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubSendLetters;
import static uk.gov.hmcts.divorce.testutil.SendLetterWireMock.stubStatusOfSendLetter;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class,
    CdamWireMock.PropertiesInitializer.class,
    SendLetterWireMock.PropertiesInitializer.class
})
public class SubmitAosIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CcdUpdateService ccdUpdateService;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
        CdamWireMock.start();
        SendLetterWireMock.start();
    }

    @BeforeEach
    void setClock() {
        LocalDateTime dateTime = LocalDateTime.of(2022, Month.FEBRUARY, 15, 13, 39);
        Instant instant = dateTime.atZone(ZoneId.of("Europe/London")).toInstant();
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.of("Europe/London"));
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
        CdamWireMock.stopAndReset();
        SendLetterWireMock.stopAndReset();
    }

    @Test
    void shouldSetStateToHoldingAndSetDateAosSubmittedAndGenerateRespondentPdfForValidUndisputedAos() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setSupplementaryCaseType(NA);
        caseData.setApplicationType(SOLE_APPLICATION);

        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse("classpath:solicitor-submit-aos-response.json"));
    }

    @ParameterizedTest
    @MethodSource("caseStateParameters")
    void shouldSetStateToHoldingForValidUndisputedAosWithValidAosPrestates(State aosValidState) throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplicant2().setLegalProceedings(NO);
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setSupplementaryCaseType(NA);
        caseData.setApplicationType(SOLE_APPLICATION);

        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS, aosValidState.name())))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.state").value("Holding"));
    }

    @Test
    void shouldSetStateToHoldingAndSetDateAosSubmittedAndGenerateRespondentPdfInWelshForValidUndisputedAos() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setSupplementaryCaseType(NA);
        caseData.setApplicationType(SOLE_APPLICATION);

        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Cy.docx");

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenValidCaseDataWithoutDisputeWhenAboutToSubmitCallbackIsInvokedThenSuccess() throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.setSupplementaryCaseType(NA);
        data.setApplicationType(SOLE_APPLICATION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulWithoutDisputeResponse()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLICANT_AOS_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_AOS_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    public void givenValidCaseDataWithDisputeWhenAboutToSubmitCallbackIsInvokedThenSuccess() throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LOCAL_DATE);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.setSupplementaryCaseType(NA);
        data.setApplicationType(SOLE_APPLICATION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulWithDisputeResponse()));

        verify(notificationService).sendEmail(eq("test@test.com"), any(), any(), any(), any());
        verify(notificationService).sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), any(), any(), any(), any());
    }

    @Test
    public void givenValidCaseDataWithDisputeWhenAboutToSubmitCallbackIsInvokedThenSuccessWhenLangPrefIsWelsh()
        throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LOCAL_DATE);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        data.setSupplementaryCaseType(NA);
        data.setApplicationType(SOLE_APPLICATION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(expectedCcdAboutToStartCallbackSuccessfulWithDisputeResponseWelsh());

        assertThatJson(actualResponse)
            .isEqualTo(jsonDocument.json());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED), anyMap(), eq(WELSH), anyLong());
    }

    @Test
    public void givenValidCaseDataForRespondentRepresentedWhenAboutToSubmitCallbackIsInvokedThenSubmitAos() throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplicant2().setLegalProceedings(NO);
        data.setSupplementaryCaseType(NA);
        data.setApplicationType(SOLE_APPLICATION);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulWithRepresentedRespondent()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLICANT_AOS_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(SOLE_AOS_SUBMITTED_RESPONDENT_SOLICITOR), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldGenerateAndSendAosResponseLetterWhenApplicant1IsOfflineAndAosIsDisputed() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .howToRespondApplication(DISPUTE_DIVORCE)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.setSupplementaryCaseType(NA);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.getDocuments().setScannedDocuments(singletonList(aosScannedDocument()));
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");
        UUID d84Uuid = UUID.randomUUID();
        caseData.getDocuments().setDocumentsGenerated(
            new ArrayList<>(List.of(
                ListValue.<DivorceDocument>builder().id(d84Uuid.toString())
                    .value(DivorceDocument.builder()
                        .documentLink(Document.builder()
                            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/%s"
                                .formatted(d84Uuid.toString()))
                            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/%s/binary"
                                .formatted(d84Uuid.toString()))
                            .filename("d84.pdf")
                            .build())
                        .documentType(DocumentType.D84)
                        .documentFileName("d84.pdf")
                        .build()).build())));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");
        stubForDocAssemblyWith("baf61f9a-38e5-11ed-a261-0242ac120002", "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith("baf61f9a-38e5-11ed-a261-0242ac120002", "FL-NFD-GOR-ENG-Respondent-Responded-Defended.docx");
        stubAosPackSendLetter(d84Uuid.toString());

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse("classpath:solicitor-submit-aos-disputed-offline-response.json"));
    }

    @Test
    void shouldGenerateAndSendAosResponseLetterWhenApplicant1IsOfflineAndContactIsPrivateAndAosIsDisputed() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .howToRespondApplication(DISPUTE_DIVORCE)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.setSupplementaryCaseType(NA);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.getDocuments().setScannedDocuments(singletonList(aosScannedDocument()));
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");
        stubForDocAssemblyWith("baf61f9a-38e5-11ed-a261-0242ac120002", "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith("51afe8e5-0061-42b6-83a2-4c122046901c", "FL-NFD-GOR-ENG-Respondent-Responded-Defended.docx");
        stubAosPackSendLetter();

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse("classpath:solicitor-submit-aos-offline-response-with-private-contact.json"));
    }

    @Test
    void shouldGenerateAndSendAosResponseLetterWhenApplicant1IsOfflineAndContactIsPrivateAndAosIsUndisputed() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .howToRespondApplication(WITHOUT_DISPUTE_DIVORCE)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.setSupplementaryCaseType(NA);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.getDocuments().setScannedDocuments(singletonList(aosScannedDocument()));
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");
        stubForDocAssemblyWith("baf61f9a-38e5-11ed-a261-0242ac120002", "NFD_Applicant_Coversheet.docx");
        stubForDocAssemblyWith("51afe8e5-0061-42b6-83a2-4c122046901c", "FL-NFD-GOR-ENG-Respondent-Responded-Undefended.docx");
        stubAosPackSendLetter();

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        DocumentContext jsonDocument = JsonPath.parse(expectedResponse(
            "classpath:solicitor-submit-aos-offline-response-with-private-contact.json"));

        jsonDocument.set("data.howToRespondApplication", "withoutDisputeDivorce");

        assertThatJson(jsonStringResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(jsonDocument.json());
    }

    @Test
    public void givenValidCaseDataForRespondentRepresentedWhenSubmittedCallbackIsInvokedThenSendEmailToRespSolicitor() throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.setSupplementaryCaseType(NA);
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplicant2().setLegalProceedings(NO);
        data.setDueDate(LOCAL_DATE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(data);
        caseDetails.setId(1L);

        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());

        doNothing().when(ccdUpdateService).submitEvent(1L, SYSTEM_ISSUE_AOS_UNDISPUTED, user, TEST_SERVICE_AUTH_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS, AosDrafted.name())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(ccdUpdateService).submitEvent(any(), eq(SYSTEM_ISSUE_AOS_UNDISPUTED), any(), eq(TEST_SERVICE_AUTH_TOKEN));
    }

    private ListValue<ScannedDocument> aosScannedDocument() {
        return ListValue.<ScannedDocument>builder()
            .value(
                ScannedDocument
                    .builder()
                    .url(
                        Document
                            .builder()
                            .url("http://localhost:8080/4cacfcd1-3588-40c2-94da-c22fb59e1068")
                            .binaryUrl("http://localhost:8080/4cacfcd1-3588-40c2-94da-c22fb59e1068/binary")
                            .build()
                    )
                    .subtype("aos")
                    .build())
            .build();
    }

    private String expectedCcdAboutToStartCallbackSuccessfulWithoutDisputeResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-citizen-submit-aos-without-dispute.json");
    }

    private String expectedCcdAboutToStartCallbackSuccessfulWithDisputeResponse() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-citizen-submit-aos-with-dispute.json");
    }

    private String expectedCcdAboutToStartCallbackSuccessfulWithDisputeResponseWelsh() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-citizen-submit-aos-with-dispute-welsh.json");
    }

    private String expectedCcdAboutToStartCallbackSuccessfulWithRepresentedRespondent() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-citizen-submit-aos-represented-respondent.json");
    }

    private void stubAosPackSendLetter() throws IOException {
        stubAosPackSendLetter(List.of(
            "51afe8e5-0061-42b6-83a2-4c122046901c", // coversheet
            "c35b1868-e397-457a-aa67-ac1422bb8100", // NOP document id
            "baf61f9a-38e5-11ed-a261-0242ac120002" // Scanned document id
        ));
    }

    private void stubAosPackSendLetter(String uuid) throws IOException {
        final List<String> documentIds = List.of(
            "51afe8e5-0061-42b6-83a2-4c122046901c", // coversheet
            "c35b1868-e397-457a-aa67-ac1422bb8100", // NOP document id
            "baf61f9a-38e5-11ed-a261-0242ac120002", // Scanned document id
            uuid //d84 document id
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
    }

    private byte[] loadPdfAsBytes() throws IOException {
        return resourceAsBytes("classpath:Test.pdf");
    }

    private static Stream<Arguments> caseStateParameters() {
        return Arrays.stream(ArrayUtils.addAll(AOS_STATES, AosDrafted, AosOverdue, OfflineDocumentReceived, AwaitingService))
            .filter(state -> !AwaitingConditionalOrder.equals(state))
            .map(Arguments::of);
    }
}
