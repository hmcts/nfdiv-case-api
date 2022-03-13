package uk.gov.hmcts.divorce.common;

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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.SendLetterWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.LetterStatus;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitAos.SUBMIT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.HowToRespondApplication.WITHOUT_DISPUTE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
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
    DocManagementStoreWireMock.PropertiesInitializer.class,
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

    @Test
    void shouldSetStateToHoldingAndSetDateAosSubmittedAndGenerateRespondentPdfForValidUndisputedAos() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .prayerHasBeenGiven(YES)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);

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
                        callbackRequest(caseData, SUBMIT_AOS)))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse("classpath:solicitor-submit-aos-response.json"));
    }

    @Test
    public void givenValidCaseDataWithoutDisputeWhenCallbackIsInvokedThenSendEmailToApplicantAndRespondent() throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getAcknowledgementOfService().setHowToRespondApplication(WITHOUT_DISPUTE_DIVORCE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulWithoutDisputeResponse()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLICANT_AOS_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_AOS_SUBMITTED), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWithDisputeWhenCallbackIsInvokedThenSendEmailToApplicantAndRespondent() throws Exception {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LOCAL_DATE);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getAcknowledgementOfService().setHowToRespondApplication(DISPUTE_DIVORCE);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest(data, SUBMIT_AOS)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessfulWithDisputeResponse()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLICANT_DISPUTED_AOS_SUBMITTED), anyMap(), eq(ENGLISH));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_DISPUTED_AOS_SUBMITTED), anyMap(), eq(ENGLISH));

        verifyNoMoreInteractions(notificationService);
    }


    @Test
    void shouldGenerateAndSendAosResponseLetterWhenApplicant1IsOfflineAndAosIsDisputed() throws Exception {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .statementOfTruth(YES)
            .prayerHasBeenGiven(YES)
            .howToRespondApplication(DISPUTE_DIVORCE)
            .confirmReadPetition(YES)
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setScannedDocuments(singletonList(aosScannedDocument()));
        caseData.getApplicant2().setLegalProceedings(YES);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setLegalProceedingsDetails("some description");

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);
        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("c35b1868-e397-457a-aa67-ac1422bb8100", "NFD_Respondent_Answers_Eng.docx");
        stubForDocAssemblyWith("51afe8e5-0061-42b6-83a2-4c122046901c", "NFD_Respondent_Responded_Disputed.docx");
        stubAosPackSendLetter();

        final var jsonStringResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(
                    objectMapper.writeValueAsString(
                        callbackRequest(caseData, SUBMIT_AOS)))
                .accept(APPLICATION_JSON))
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(jsonStringResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(expectedResponse("classpath:solicitor-submit-aos-disputed-offline-response.json"));
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

    private void stubAosPackSendLetter() throws IOException {
        final List<String> documentIds = List.of(
            "51afe8e5-0061-42b6-83a2-4c122046901c", // NOP document id
            "4cacfcd1-3588-40c2-94da-c22fb59e1068"  // Scanned document id
        );

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
    }

    private byte[] loadPdfAsBytes() throws IOException {
        return resourceAsBytes("classpath:Test.pdf");
    }

}
