package uk.gov.hmcts.divorce.legaladvisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.ADMIN_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class, DocAssemblyWireMock.PropertiesInitializer.class})
@AutoConfigureMockMvc
@DirtiesContext
public class LegalAdvisorMakeDecisionIT {

    private static final String REJECTED_REFUSAL_ORDER_TEMPLATE_FILE_NAME = "FL-NFD-GOR-ENG-Conditional-Order-Refusal-Order.docx";
    private static final String CLARIFICATION_REFUSAL_ORDER_TEMPLATE_FILE_NAME =
        "FL-NFD-GOR-ENG-Conditional-Order-Clarification-Refusal-Order-Offline.docx";
    private static final String CLARIFICATION_REFUSAL_ORDER_WELSH_TEMPLATE_FILE_NAME =
        "FL-NFD-GOR-WEL-Conditional-Order-Clarification-Refusal-Order.docx";
    private static final String UUID = "49fa338b-1955-41c2-8e05-1df710a8ffaa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Clock clock;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private NotificationService notificationService;

    @Mock
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

    @Mock
    private ConditionalOrderRefusedForClarificationContent conditionalOrderRefusedForClarificationContent;

    @BeforeAll
    static void setUp() {
        DocAssemblyWireMock.start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocAssemblyWireMock.stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @Test
    public void shouldSetDecisionDateAndStateToAwaitingPronouncementWhenConditionalOrderIsGranted() throws Exception {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(YES)
            .claimsGranted(NO)
            .build());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.data.coDecisionDate").value(getExpectedLocalDate().toString())
            )
            .andExpect(
                jsonPath("$.state").value(AwaitingPronouncement.name())
            );
    }

    @Test
    public void shouldSetStateToAwaitingClarificationIfConditionalOrderIsNotGrantedAndRefusalIsDueToMoreInformationRequired()
        throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build());

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(AwaitingClarification.name())
            );
    }

    @Test
    public void givenConditionalOrderIsNotGrantedAndRefusalIsDueToMoreInformationRequiredThenShouldSendNotification()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build());

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, CLARIFICATION_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    LEGAL_ADVISOR_MAKE_DECISION)
                )
            )
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    public void givenCaseTypeIsJointAndConditionalOrderIsNotGrantedAndLanguagePreferenceIsWelshThenShouldSendWelshNotifications()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, CLARIFICATION_REFUSAL_ORDER_WELSH_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    LEGAL_ADVISOR_MAKE_DECISION)
                )
            )
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO)
            )),
            eq(WELSH)
        );

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO)
            )),
            eq(WELSH)
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSetStateToAwaitingAdminClarificationIfConditionalOrderIsNotGrantedAndRefusalIsDueToAdminError()
        throws Exception {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(ADMIN_ERROR)
            .build());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(AwaitingAdminClarification.name())
            );
    }

    @Test
    public void shouldSetStateToAwaitingAmendedApplicationAnsSendNotificationToApplicantIfConditionalOrderIsRejectedForAmendment()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .build());
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, REJECTED_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(AwaitingAmendedApplication.name())
            );

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSetStateToAwaitingAmendedApplicationAnsSendNotificationToBothApplicantsIfConditionalOrderIsRejectedForAmendment()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .build());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, REJECTED_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(AwaitingAmendedApplication.name())
            );

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(ENGLISH)
        );

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendNotificationInWelshToBothApplicantsIfConditionalOrderIsRejectedForAmendmentAndLanguagePrefferedIsWelsh()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .build());
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, "FL-NFD-GOR-WEL-Conditional-Order-Refusal-Order.docx");

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(AwaitingAmendedApplication.name())
            );

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(WELSH)
        );

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED_FOR_AMENDMENT),
            anyMap(),
            eq(WELSH)
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldAddLegalAdvisorDecisionToAuditList() throws Exception {

        setMockClock(clock);

        final CaseData caseData = validApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build());

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, CLARIFICATION_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    LEGAL_ADVISOR_MAKE_DECISION)
                )
            )
            .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(expectedResponse(("classpath:legal-advisor-make-decision-response.json")));

    }

    @Test
    public void givenSoleApplicationCoIsNotGrantedAndRefusalIsDueToMoreInfoRequiredThenShouldSendNotificationToApp1Solicitor()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 6, 22));
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
                .name("App1 sol")
                .email("sol1@gm.com")
                .reference("sol1")
            .build());

        caseData.getApplicant1().setSolicitorRepresented(YES);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build());

        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, CLARIFICATION_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService).sendEmail(
            eq("sol1@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            argThat(allOf(
                hasEntry("solicitor name", "App1 sol"),
                hasEntry("solicitor reference", "sol1"),
                hasEntry("applicant1Label", "Applicant"),
                hasEntry("applicant2Label", "Respondent"),
                hasEntry("isJoint", "no"),
                hasEntry("moreInfo", "yes"),
                hasEntry("amendApplication", "no"),
                hasEntry("issueDate", "22 June 2022")
            )),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void givenJointCaseCoIsNotGrantedAndRefusalIsDueToAmendApplicationThenShouldSendNotificationToBothApplicantSolicitors()
        throws Exception {

        setMockClock(clock);

        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setIssueDate(LocalDate.of(2022, 6, 22));
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("App1 sol")
            .email("sol1@gm.com")
            .reference("sol1")
            .build());

        caseData.getApplicant1().setSolicitorRepresented(YES);

        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("App2 sol")
            .email("sol2@gm.com")
            .reference("sol2")
            .build());

        caseData.getApplicant2().setSolicitorRepresented(YES);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .build());

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, REJECTED_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService).sendEmail(
            eq("sol1@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            argThat(allOf(
                hasEntry("solicitor name", "App1 sol"),
                hasEntry("solicitor reference", "sol1"),
                hasEntry("applicant1Label", "Applicant 1"),
                hasEntry("applicant2Label", "Applicant 2"),
                hasEntry("isJoint", "yes"),
                hasEntry("moreInfo", "no"),
                hasEntry("amendApplication", "yes"),
                hasEntry("issueDate", "22 June 2022")
            )),
            eq(ENGLISH)
        );

        verify(notificationService).sendEmail(
            eq("sol2@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            argThat(allOf(
                hasEntry("solicitor name", "App2 sol"),
                hasEntry("solicitor reference", "sol2"),
                hasEntry("applicant1Label", "Applicant 1"),
                hasEntry("applicant2Label", "Applicant 2"),
                hasEntry("isJoint", "yes"),
                hasEntry("moreInfo", "no"),
                hasEntry("amendApplication", "yes"),
                hasEntry("issueDate", "22 June 2022")
            )),
            eq(ENGLISH)
        );

        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void midEventShouldGenerateRefusalOrderDocumentWhenCORejectedWithMoreInfo() throws Exception {
        setMockClock(clock);
        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(MORE_INFO)
            .refusalClarificationReason(Set.of(MARRIAGE_CERTIFICATE))
            .refusalClarificationAdditionalInfo("Clarification comments")
            .build());

        when(conditionalOrderRefusedForClarificationContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, CLARIFICATION_REFUSAL_ORDER_TEMPLATE_FILE_NAME);

        mockMvc.perform(post(CO_REFUSAL_ORDER_WITH_MORE_INFO_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.data.coRefusalOrderDocument.document_filename").value("NoticeOfRefusalDocument.pdf")
            );
    }

    @Test
    public void midEventShouldGenerateRefusalOrderDocumentWhenCORejectedWithAmendments() throws Exception {
        setMockClock(clock);
        final Map<String, Object> templateContent = new HashMap<>();
        final CaseData caseData = validApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .granted(NO)
            .refusalDecision(REJECT)
            .build());

        when(conditionalOrderRefusedForAmendmentContent.apply(caseData, TEST_CASE_ID))
            .thenReturn(templateContent);
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith(UUID, "NFD_Refusal_Order_V2.docx");

        mockMvc.perform(post(CO_REFUSAL_ORDER_WITH_AMENDMENTS_MID_EVENT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_MAKE_DECISION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.data.coRefusalOrderDocument.document_filename").value("NoticeOfRefusalDocument.pdf")
            );
    }
}
