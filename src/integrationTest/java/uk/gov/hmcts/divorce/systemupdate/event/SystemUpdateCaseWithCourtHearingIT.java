package uk.gov.hmcts.divorce.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.DocumentConstants;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.DocTemplateResolver;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseWithCourtHearing;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class
})
public class SystemUpdateCaseWithCourtHearingIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocTemplateResolver docTemplateResolver;

    @MockitoBean
    private RequestInterceptor requestInterceptor;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockitoBean
    private BulkPrintService bulkPrintService;

    @Captor
    private ArgumentCaptor<Print> printCaptor;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        DocAssemblyWireMock.start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicantAndRespondent() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccess()));

        verify(notificationService, times(4))
            .sendEmail(any(), any(), anyMap(), eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenBothApplicantsHaveWelshAsChosenLanguageThenSendWelshEmails() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID, WELSH);

        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(notificationService, times(4))
                .sendEmail(any(), any(), anyMap(), eq(WELSH),
                        anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmailToApplicantsForJointCase() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementDoNotAttendCourtTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_DO_NOT_ATTEND_COURT_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("6dd725e8-f053-4493-9cbe-bb69d1905a12",
            certificateOfEntitlementDoNotAttendCourtTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedCcdAboutToSubmitCallbackSuccess()));

        verify(notificationService, times(4))
            .sendEmail(any(), any(), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenBothApplicantsAreRepresentedAndIsDigitalApplicationSendEmailToBothApplicantSolicitors() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .email(TEST_SOLICITOR_EMAIL)
                .reference("App1 Ref")
                .build()
        );
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .email("app2-solicitor@test.com")
                .reference("App2 Ref")
                .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq("app2-solicitor@test.com"), eq(SOLICITOR_CONDITIONAL_ORDER_ENTITLEMENT_GRANTED), anyMap(), eq(ENGLISH),
                anyLong());

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenBothApplicantsAreRepresentedAndIsPaperApplicationThenNoEmailsSent() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementSolicitorCoverLetterTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3",
            certificateOfEntitlementSolicitorCoverLetterTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setContactDetailsType(PUBLIC);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .email(TEST_SOLICITOR_EMAIL)
                .reference("App1 Ref")
                .build()
        );
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setEmail("");
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .email("app2-solicitor@test.com")
                .reference("App2 Ref")
                .build()
        );

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        verify(bulkPrintService, times(2)).print(any());
        verifyNoMoreInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenSoleApplicationWithOfflineRespondentThenNoEmailsSent() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementCoverLetterTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID);
        final String certificateOfEntitlementCoverLetterOfflineRespondentTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID);
        final String certificateOfEntitlementDoNotAttendCourtTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_DO_NOT_ATTEND_COURT_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae1", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae2", certificateOfEntitlementCoverLetterTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905d33",
            certificateOfEntitlementCoverLetterOfflineRespondentTemplateId);
        stubForDocAssemblyWith("6dd725e8-f053-4493-9cbe-bb69d1905a12",
            certificateOfEntitlementDoNotAttendCourtTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setOffline(NO);
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setSolicitorRepresented(NO);

        when(bulkPrintService.print(printCaptor.capture())).thenReturn(UUID.randomUUID());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService, times(2))
                .sendEmail(any(), any(), anyMap(), eq(ENGLISH),
                        anyLong());

        verifyNoMoreInteractions(notificationService);

        verify(bulkPrintService).print(any());
        verifyNoMoreInteractions(bulkPrintService);

        final Print print = printCaptor.getValue();
        assertThat(print.getCaseId()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getCaseRef()).isEqualTo(TEST_CASE_ID.toString());
        assertThat(print.getLetterType()).isEqualTo("certificate-of-entitlement");
        assertThat(print.getLetters()).hasSize(3);
    }

    @Test
    public void givenBothApplicantsOfflineWhenSoleJudicialSeparationCaseThenJSCoverLettersAreGenerated() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementJudicialSeparationTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID);
        final String certificateOfEntitlementJsCoverLetterTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3",
            certificateOfEntitlementJudicialSeparationTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae1", certificateOfEntitlementJsCoverLetterTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setFirstName("Applicant1");
        data.getApplicant1().setEmail(null);
        data.getApplicant1().setContactDetailsType(PUBLIC);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setFirstName("Applicant2");
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setSolicitorRepresented(NO);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(bulkPrintService, times(2)).print(any());
        verifyNoMoreInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenBothApplicantsOfflineWhenJointJudicialSeparationCaseThenJSCoverLettersAreGenerated() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementJudicialSeparationTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID);
        final String certificateOfEntitlementJsCoverLetterTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3",
            certificateOfEntitlementJudicialSeparationTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae1", certificateOfEntitlementJsCoverLetterTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setEmail(null);
        data.getApplicant1().setContactDetailsType(PUBLIC);
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setSolicitorRepresented(NO);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(bulkPrintService, times(2)).print(any());
        verifyNoMoreInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenBothApplicantsOfflineAndRepresentedWhenSoleJudicialSeparationCaseThenJSCoverLettersAreGenerated() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementJsSolicitorCoverLetterTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae1",
            certificateOfEntitlementJsSolicitorCoverLetterTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        data.setApplicationType(ApplicationType.SOLE_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setEmail(null);
        data.getApplicant1().setContactDetailsType(PUBLIC);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setSolicitorRepresented(YES);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(bulkPrintService, times(2)).print(any());
        verifyNoMoreInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void givenBothApplicantsOfflineAndRepresentedWhenJointJudicialSeparationCaseThenJSCoverLettersAreGenerated() throws Exception {
        final String certificateOfEntitlementTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID);
        final String certificateOfEntitlementJsSolicitorCoverLetterTemplateId =
            docTemplateResolver.resolveTemplateID(DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", certificateOfEntitlementTemplateId);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae1",
            certificateOfEntitlementJsSolicitorCoverLetterTemplateId);

        CaseData data = validCaseWithCourtHearing();
        data.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        data.setApplicationType(ApplicationType.JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.now());
        data.getApplicant1().setOffline(YES);
        data.getApplicant1().setEmail(null);
        data.getApplicant1().setContactDetailsType(PUBLIC);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant2().setOffline(YES);
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setSolicitorRepresented(YES);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SYSTEM_UPDATE_CASE_COURT_HEARING)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(bulkPrintService, times(2)).print(any());
        verifyNoMoreInteractions(bulkPrintService);
        verifyNoInteractions(notificationService);
    }

    private String expectedCcdAboutToSubmitCallbackSuccess() throws IOException {
        return expectedResponse("classpath:wiremock/responses/about-to-submit-system-update-case-court-hearing.json");
    }
}
