package uk.gov.hmcts.divorce.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ENFORCEMENT;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_BAILIFF;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    FeesWireMock.PropertiesInitializer.class,
})
public class SystemProgressCaseToAosOverdueIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RequestInterceptor requestInterceptor;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private LetterPrinter letterPrinter;

    @MockBean
    private CaseDataDocumentService caseDataDocumentService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        FeesWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        FeesWireMock.stopAndReset();
    }

    @Test
    public void givenValidCitizenCaseDataWhenCallbackIsInvokedThenSendEmailToApplicantAndRespondent() throws Exception {
        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplication().setSolSignStatementOfTruth(null);
        data.setDueDate(LOCAL_DATE);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_USER_EMAIL, "1234-1234-1234-1234", null));
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_PROGRESS_TO_AOS_OVERDUE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccess()));

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);

        verifyNoMoreInteractions(caseDataDocumentService);

        verifyNoMoreInteractions(letterPrinter);
    }

    @Test
    public void givenApplicant1IsOfflineWhenCallbackIsInvokedThenGenerateAosOverdueDoc()
        throws Exception {

        String responseJson = TestDataHelper.getFeeResponseAsJson();

        stubForFeesLookup(responseJson, EVENT_ENFORCEMENT, SERVICE_OTHER, KEYWORD_BAILIFF);
        stubForFeesLookup(responseJson, "general%20application", SERVICE_OTHER, KEYWORD_WITHOUT_NOTICE);

        CaseData data = validCaseDataForIssueApplication();
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant1().setOffline(YES);
        data.getApplication().setSolSignStatementOfTruth(null);
        data.setDueDate(LOCAL_DATE);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setCaseInvite(new CaseInvite(TEST_APPLICANT_2_USER_EMAIL, "1234-1234-1234-1234", null));
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_PROGRESS_TO_AOS_OVERDUE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessOffline()));

        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_APPLICATION_ACCEPTED), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenApplicant1IsOfflineAndIsJudicialSeparationWhenCallbackIsInvokedThenGenerateAosOverdueDocForJS()
        throws Exception {

        String responseJson = TestDataHelper.getFeeResponseAsJson();

        stubForFeesLookup(responseJson, EVENT_ENFORCEMENT, SERVICE_OTHER, KEYWORD_BAILIFF);
        stubForFeesLookup(responseJson, "general%20application", SERVICE_OTHER, KEYWORD_WITHOUT_NOTICE);

        CaseData data = validCaseDataForIssueApplication();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        data.getApplicant1().setSolicitor(null);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant1().setOffline(YES);
        data.getApplicant2().setOffline(YES);
        data.getApplicant1().setEmail(null);
        data.getApplicant2().setEmail(null);
        data.getApplication().setSolSignStatementOfTruth(null);
        data.setDueDate(LOCAL_DATE);
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.setCaseInvite(new CaseInvite(null, "1234-1234-1234-1234", null));

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_PROGRESS_TO_AOS_OVERDUE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(TREATING_NULL_AS_ABSENT)
            .isEqualTo(json(expectedCcdAboutToStartCallbackSuccessOfflineJS()));

        verifyNoInteractions(notificationService);
    }

    private String expectedCcdAboutToStartCallbackSuccess() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-system-progress-case-to-aos-overdue.json");
    }

    private String expectedCcdAboutToStartCallbackSuccessOffline() throws IOException {
        return expectedResponse(
            "classpath:wiremock/responses/about-to-submit-system-progress-case-to-aos-overdue-offline.json");
    }

    private String expectedCcdAboutToStartCallbackSuccessOfflineJS() throws IOException {
        return expectedResponse(
            "classpath:wiremock/responses/about-to-submit-system-progress-case-to-aos-overdue-offline-js.json");
    }
}
