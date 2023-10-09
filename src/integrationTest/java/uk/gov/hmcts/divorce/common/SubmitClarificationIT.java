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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;

import static java.util.Collections.singletonList;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitClarification.SUBMIT_CLARIFICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_PARTNER_CLARIFICATION_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.POST_INFORMATION_TO_COURT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CITIZEN_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SOLICITOR_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_2_CITIZEN_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmitClarificationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSendEmailToApplicant1IfCannotUploadClarificationDocuments() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(POST_INFORMATION_TO_COURT), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldSendEmailToApplicant2IfCannotUploadClarificationDocumentsAndJoint() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(POST_INFORMATION_TO_COURT), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldNotSendPostInformationNotificationIfCanUploadClarificationDocuments() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(NO);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldChangeCaseStateToClarificationSubmitted() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant1CaseData();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.state").value("ClarificationSubmitted"));
    }

    @Test
    public void shouldAddUploadedClarificationDocumentsToUploadedDocumentsListAndPopulateAuditList()
        throws Exception {

        setMockClock(clock);

        Document ccdDocument = new Document(
            "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27",
            "clarification.pdf",
            "http://dm-store-aat.service.core-compute-aat.internal/documents/8d2bd0f2-80e9-4b0f-b38d-2c138b243e27/binary"
        );

        DivorceDocument divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName("clarification.pdf")
            .documentType(MARRIAGE_CERTIFICATE_TRANSLATION)
            .build();

        ListValue<DivorceDocument> divorceDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(MARRIAGE_CERTIFICATE_TRANSLATION.getLabel())
            .value(divorceDocument)
            .build();

        final CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setClarificationResponses(
            singletonList(
                ListValue.<String>builder()
                    .value("A clarification")
                    .build()
            )
        );
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(NO);
        caseData.getConditionalOrder().setClarificationUploadDocuments(singletonList(divorceDocumentListValue));

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, SOLICITOR_USER_ID, SOLICITOR_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        String actualResponse = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(("classpath:solicitor-submit-clarification-response.json")));
    }

    @Test
    void shouldSendEmailToApplicant1IfSubmittedClarificationForSoleCase() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldSendEmailInWelshToApplicant1IfSubmittedClarificationForSoleCase() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(WELSH), anyLong());
    }


    @Test
    void shouldSendEmailToApplicant1AndApplicant2IfClarificationSubmittedByApplicant1ForJointCase() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldSendEmailInWelshToApplicant1AndApplicant2IfClarificationSubmittedByApplicant1ForJointCase() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(WELSH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED), anyMap(), eq(WELSH), anyLong());
    }

    @Test
    void shouldSendEmailToApplicant1AndApplicant2IfClarificationSubmittedByApplicant2ForJointCase() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, APP_2_CITIZEN_USER_ID, CITIZEN_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldSendEmailInWelshToApplicant1AndApplicant2IfClarificationSubmittedByApplicant2ForJointCase() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, APP_2_CITIZEN_USER_ID, CITIZEN_ROLE);
        stubForIdamToken(TEST_AUTHORIZATION_TOKEN);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CLARIFICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(CITIZEN_CLARIFICATION_SUBMITTED), anyMap(), eq(WELSH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_PARTNER_CLARIFICATION_SUBMITTED), anyMap(), eq(WELSH), anyLong());
    }
}
