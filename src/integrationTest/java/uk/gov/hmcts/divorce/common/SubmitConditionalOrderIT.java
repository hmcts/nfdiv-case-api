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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.content.templatecontent.ConditionalOrderAnswersTemplateContent;
import uk.gov.hmcts.divorce.notification.EmailTemplateName;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.common.event.SubmitConditionalOrder.SUBMIT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getFormattedExpectedDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SubmitConditionalOrderIT {

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

    @MockBean
    private ConditionalOrderAnswersTemplateContent templateContentService;

    @MockBean
    private DocumentGenerator documentGenerator;

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
    void shouldSetDateSubmitted() throws Exception {

        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.coApplicant1SubmittedDate").value(getFormattedExpectedDateTime()));
    }

    @Test
    public void shouldSendApplicant1NotificationWhenSoleApplication() throws Exception {

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void shouldSetDateSubmittedAndSendWelshNotification() throws Exception {

        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-WEL-Conditional_Order_Answers.docx");

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.coApplicant1SubmittedDate").value(getFormattedExpectedDateTime()));
    }

    @Test
    public void shouldSendApplicant1NotificationInWelshWhenSoleApplication() throws Exception {

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(WELSH), anyLong());
    }

    @Test
    void shouldReturnAnErrorIfSoTNotCompleted() throws Exception {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.NO).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors[0]")
                .value("The applicant must agree that the facts stated in the application are true"));
    }

    @Test
    public void testJointApplicant1SubmitsCOAndApplicant2StillNotApplied()
        throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        verifyJointCOSubmission(
            ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build()
        );
    }

    @Test
    public void shouldSendEmailToBothApplicantsWhenJointApplicant1SubmitsCOAndApplicant2StillNotApplied()
        throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        verifyJointCONotifications(
            ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build(),
            JOINT_APPLIED_FOR_CONDITIONAL_ORDER,
            JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER
        );
    }

    @Test
    void testJointApplicant2SubmitsCOAndApplicant1StillNotApplied() throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        verifyJointCOSubmission(
            ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build()
        );
    }

    @Test
    void shouldSendEmailToBothApplicantsWhenJointApplicant2SubmitsCOAndApplicant1StillNotApplied() throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        verifyJointCONotifications(
            ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build(),
            JOINT_PARTNER_APPLIED_FOR_CONDITIONAL_ORDER,
            JOINT_APPLIED_FOR_CONDITIONAL_ORDER
        );
    }

    @Test
    public void testJointApplicant1SubmitsCOAndApplicant2HaveAlreadyApplied()
        throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        verifyJointCOSubmission(
            ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build()
        );
    }

    @Test
    public void shouldSendBothApplicantsAppliedForCOEmailToBothApplicantsWhenJointApplicant1SubmitsCOAndApplicant2HaveAlreadyApplied()
        throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        verifyJointCONotifications(
            ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build(),
            JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER,
            JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER
        );
    }

    @Test
    public void testJointApplicant2SubmitsCOAndApplicant1HaveAlreadyApplied()
        throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        verifyJointCOSubmission(
            ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build()
        );
    }

    @Test
    public void shouldSendBothApplicantsAppliedForCOEmailToBothApplicantsWhenJointApplicant2SubmitsCOAndApplicant1HaveAlreadyApplied()
        throws Exception {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        verifyJointCONotifications(
            ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
                .build(),
            JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER,
            JOINT_BOTH_APPLIED_FOR_CONDITIONAL_ORDER
        );
    }

    @Test
    void testJointConditionalOrderIsSubmittedByApplicant1Solicitor() throws Exception {

        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("app1sol")
            .email("app1sol@gm.com")
            .reference("refxxx")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.coApplicant1SubmittedDate").value(getFormattedExpectedDateTime()));
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWhenJointConditionalOrderIsSubmittedByApplicant1Solicitor() throws Exception {

        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("app1sol")
            .email("app1sol@gm.com")
            .reference("refxxx")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq("app1sol@gm.com"), eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void testJointConditionalOrderIsSubmittedByApplicant2Solicitor() throws Exception {

        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("app2sol")
            .email("app2sol@gm.com")
            .reference("refxxx")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.coApplicant2SubmittedDate").value(getFormattedExpectedDateTime()));
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWhenJointConditionalOrderIsSubmittedByApplicant2Solicitor() throws Exception {

        setMockClock(clock);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("app2sol")
            .email("app2sol@gm.com")
            .reference("refxxx")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq("app2sol@gm.com"), eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER), anyMap(), eq(ENGLISH), anyLong());
    }

    @Test
    void testJointApplicationWithApplicant2RepresentedWhenConditionalOrderIsSubmittedByApplicant1()
        throws Exception {

        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("app1sol")
            .email("app1sol@gm.com")
            .reference("refxxx1")
            .build());

        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("app2sol")
            .email("app2sol@gm.com")
            .reference("refxxx2")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.data.coApplicant1SubmittedDate").value(getFormattedExpectedDateTime()));
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorNotifyingOtherApplicantHasAppliedForCoWhenConditionalOrderIsSubmittedByApplicant1()
        throws Exception {

        setMockClock(clock);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(true);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("app1sol")
            .email("app1sol@gm.com")
            .reference("refxxx1")
            .build());

        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("app2sol")
            .email("app2sol@gm.com")
            .reference("refxxx2")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq("app1sol@gm.com"), eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq("app2sol@gm.com"), eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH),
                anyLong());
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorNotifyingOtherApplicantHasAppliedForCoWhenConditionalOrderIsSubmittedByApplicant2()
        throws Exception {

        setMockClock(clock);

        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setIssueDate(getExpectedLocalDate());
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant1().setSolicitor(Solicitor.builder()
            .name("app1sol")
            .email("app1sol@gm.com")
            .reference("refxxx1")
            .build());

        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);
        caseData.getApplicant2().setSolicitor(Solicitor.builder()
            .name("app2sol")
            .email("app2sol@gm.com")
            .reference("refxxx2")
            .build());
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YesOrNo.YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            );

        verify(notificationService)
            .sendEmail(eq("app2sol@gm.com"), eq(JOINT_SOLICITOR_APPLIED_FOR_CO_OR_FO_ORDER), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq("app1sol@gm.com"), eq(JOINT_SOLICITOR_OTHER_PARTY_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH),
                anyLong());
    }

    private void verifyJointCOSubmission(final ConditionalOrder conditionalOrder)
        throws Exception {
        final Map<String, Object> mockedTemplateContent = new HashMap<>();

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(templateContentService.getTemplateContent(any(), any(), any())).thenReturn(mockedTemplateContent);

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail("app2@gm.com");
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(conditionalOrder);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());
    }

    private void verifyJointCONotifications(final ConditionalOrder conditionalOrder, final EmailTemplateName applicant1EmailTemplateId,
                                            final EmailTemplateName applicant2EmailTemplateId)
        throws Exception {

        setMockClock(clock);
        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setEmail("app2@gm.com");
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setConditionalOrder(conditionalOrder);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/about-to-submit?page=ConditionalOrderSoT")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SUBMIT_CONDITIONAL_ORDER)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(applicant1EmailTemplateId), anyMap(), eq(ENGLISH), anyLong());

        verify(notificationService)
            .sendEmail(eq("app2@gm.com"), eq(applicant2EmailTemplateId), anyMap(), eq(ENGLISH), anyLong());

        verifyNoMoreInteractions(notificationService);
    }
}
