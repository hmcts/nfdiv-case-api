package uk.gov.hmcts.divorce.common;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static net.javacrumbs.jsonunit.core.Option.TREATING_NULL_AS_ABSENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCoSendLetters.SWITCH_TO_SOLE_CO_SEND_LETTERS;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock.stubForDocAssemblyWith;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.CASEWORKER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.SYSTEM_USER_ROLE;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamDetails;
import static uk.gov.hmcts.divorce.testutil.IdamWireMock.stubForIdamToken;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    DocAssemblyWireMock.PropertiesInitializer.class,
    IdamWireMock.PropertiesInitializer.class})
public class SwitchedToSoleCoIT {

    private static final String SWITCH_TO_SOLE_CO_APPLICANT_2_RESPONSE =
        "classpath:switch-to-sole-co-applicant2-response.json";
    private static final String SWITCH_TO_SOLE_CO_APPLICANT_1_RESPONSE =
        "classpath:switch-to-sole-co-applicant1-response.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String BEARER_TEST_SYSTEM_AUTHORISATION_TOKEN = "Bearer " + TEST_SYSTEM_AUTHORISATION_TOKEN;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private IdamService idamService;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockBean
    private CcdUpdateService ccdUpdateService;

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
    public void shouldSwitchApplicationTypeToSole() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(true, false);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        assertThatJson(actualResponse)
            .inPath("$.data.switchedToSoleCo")
            .isEqualTo(YES);
    }

    @Test
    public void shouldSwitchApplicationTypeToSoleWhenTriggeredByApplicant2() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(false, true);

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(
            BEARER_TEST_SYSTEM_AUTHORISATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            List.of(String.valueOf(TEST_CASE_ID)))
        ).thenReturn(caseRolesResponse);

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .inPath("$.data.applicationType")
            .isEqualTo(ApplicationType.SOLE_APPLICATION);

        assertThatJson(actualResponse)
            .inPath("$.data.switchedToSoleCo")
            .isEqualTo(YES);
    }

    @Test
    public void shouldSwitchApplicationTypeToSoleAndSwitchApplicantDataIfD84SwitchToSoleTriggeredByApplicant2()
        throws Exception {

        CaseData data = validJointApplicant1CaseData();
        data.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        data.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .d84WhoApplying(APPLICANT_2)
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(false, false);

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(
            BEARER_TEST_SYSTEM_AUTHORISATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            List.of(String.valueOf(TEST_CASE_ID)))
        ).thenReturn(caseRolesResponse);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(SWITCH_TO_SOLE_CO_APPLICANT_2_RESPONSE)));
    }

    @Test
    public void shouldNotSwitchApplicantDataOnSwitchToSoleIfD84SwitchToSoleTriggeredByApplicant1()
        throws Exception {

        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setContactDetailsType(ContactDetailsType.PUBLIC);
        data.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        data.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .d84WhoApplying(APPLICANT_1)
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(false, false);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(SWITCH_TO_SOLE_CO_APPLICANT_1_RESPONSE)));
    }

    @Test
    public void shouldSendSwitchToSoleNotificationsInSubmittedCallback() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(PARTNER_SWITCHED_TO_SOLE_CO), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendSwitchToSoleNotificationsInSubmittedCallbackWelsh() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        data.getApplicant2().setLanguagePreferenceWelsh(YES);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(PARTNER_SWITCHED_TO_SOLE_CO), anyMap(), eq(WELSH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendSwitchToSoleNotificationsToSolicitorAndCitizenInSubmittedCallback() throws Exception {
        CaseData data = validJointApplicant1CaseData();
        final LocalDate issueDate = getExpectedLocalDate();
        data.getApplication().setIssueDate(issueDate);
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(Solicitor
            .builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());

        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(
                eq(TEST_SOLICITOR_EMAIL),
                eq(SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong()
            );
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldPrintSwitchToSoleCoLetterIfD84SwitchToSole() throws Exception {

        CaseData data = validJointApplicant1CaseData();
        data.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(CO_D84).build());
        data.setConditionalOrder(ConditionalOrder.builder()
            .d84ApplicationType(SWITCH_TO_SOLE)
            .d84WhoApplying(APPLICANT_2)
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        setupMocks(false, true);

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        when(caseAssignmentApi.getUserRoles(
            BEARER_TEST_SYSTEM_AUTHORISATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            List.of(String.valueOf(TEST_CASE_ID)))
        ).thenReturn(caseRolesResponse);

        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        doNothing().when(ccdUpdateService).submitEvent(1L, SWITCH_TO_SOLE_CO_SEND_LETTERS, user, TEST_SERVICE_AUTH_TOKEN);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SYSTEM_AUTHORISATION_TOKEN)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ccdUpdateService).submitEvent(any(), eq(SWITCH_TO_SOLE_CO_SEND_LETTERS), any(), eq(TEST_SERVICE_AUTH_TOKEN));
    }

    private void setupMocks(boolean isApplicant1, boolean isApplicant2) throws IOException {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(isApplicant1);
        when(ccdAccessService.isApplicant2(anyString(), anyLong())).thenReturn(isApplicant2);
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new User("system-user-token", UserInfo.builder().build()));

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new User(BEARER_TEST_SYSTEM_AUTHORISATION_TOKEN, UserInfo.builder().build()));

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");
        stubForDocAssemblyWith("2014c722-122c-4732-b583-75bad8dcedfc",
            "FL-NFD-GOR-ENG-Applied-For-Co-Switch-To-Sole_V2.docx");
    }
}
