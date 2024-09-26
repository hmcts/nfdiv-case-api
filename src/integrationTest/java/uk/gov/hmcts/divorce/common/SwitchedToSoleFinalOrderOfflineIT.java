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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrderOffline.SWITCH_TO_SOLE_FO_OFFLINE;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.FO_D36;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER;
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
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class SwitchedToSoleFinalOrderOfflineIT {

    private static final String SWITCH_TO_SOLE_FO_OFFLINE_APPLICANT_1_RESPONSE =
        "classpath:switch-to-sole-fo-offline-applicant1-response.json";
    private static final String SWITCH_TO_SOLE_FO_OFFLINE_APPLICANT_2_RESPONSE =
        "classpath:switch-to-sole-fo-offline-applicant2-response.json";

    private static final String BEARER_TEST_SYSTEM_AUTHORISATION_TOKEN = "Bearer " + TEST_SYSTEM_AUTHORISATION_TOKEN;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

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
    private NotificationService notificationService;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    public void shouldSwitchApplicationTypeToSoleAndSwitchApplicantRolesAndDataIfD84SwitchToSoleTriggeredByApplicant2()
        throws Exception {

        CaseData data = validJointApplicant1CaseData();
        data.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build());
        data.setFinalOrder(FinalOrder.builder()
            .d36ApplicationType(SWITCH_TO_SOLE)
            .d36WhoApplying(APPLICANT_2)
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
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_FO_OFFLINE, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(SWITCH_TO_SOLE_FO_OFFLINE_APPLICANT_2_RESPONSE)));
    }

    @Test
    public void shouldSwitchApplicationTypeToSoleAndSwitchApplicantDataNotRolesIfD84SwitchToSoleTriggeredByApplicant2OnNewPaperCase()
        throws Exception {

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setNewPaperCase(YES);
        data.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build());
        data.setFinalOrder(FinalOrder.builder()
            .d36ApplicationType(SWITCH_TO_SOLE)
            .d36WhoApplying(APPLICANT_2)
            .build());
        setupMocks(false, false);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_FO_OFFLINE, "AwaitingFinalOrder")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(SWITCH_TO_SOLE_FO_OFFLINE_APPLICANT_2_RESPONSE)));
    }

    @Test
    public void shouldNotSwitchApplicantDataOnSwitchToSoleIfD84SwitchToSoleTriggeredByApplicant1()
        throws Exception {

        CaseData data = validJointApplicant1CaseData();
        data.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build());
        data.setFinalOrder(FinalOrder.builder()
            .d36ApplicationType(SWITCH_TO_SOLE)
            .d36WhoApplying(APPLICANT_1)
            .build());
        setupMocks(false, false);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(data, SWITCH_TO_SOLE_FO_OFFLINE, "ConditionalOrderPending")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(TREATING_NULL_AS_ABSENT)
            .when(IGNORING_ARRAY_ORDER)
            .when(IGNORING_EXTRA_FIELDS)
            .isEqualTo(json(expectedResponse(SWITCH_TO_SOLE_FO_OFFLINE_APPLICANT_1_RESPONSE)));
    }

    @Test
    public void shouldSendSwitchToSoleNotificationsInSubmittedCallback() throws Exception {
        CaseData dataAfterSwitchToSoleFo = validJointApplicant1CaseData();
        dataAfterSwitchToSoleFo.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        dataAfterSwitchToSoleFo.setFinalOrder(FinalOrder.builder()
            .doesApplicant1IntendToSwitchToSole(YES)
            .dateFinalOrderSubmitted(LocalDateTime.now())
            .build());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(dataAfterSwitchToSoleFo, SWITCH_TO_SOLE_FO_OFFLINE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_APPLICANT_2_USER_EMAIL), eq(PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    public void shouldSendSwitchToSoleNotificationsInSubmittedCallbackWhenApp2Represented() throws Exception {
        CaseData dataAfterSwitchToSoleFo = validJointApplicant1CaseData();
        dataAfterSwitchToSoleFo.getApplication().setIssueDate(LocalDate.now());
        dataAfterSwitchToSoleFo.getApplicant2().setSolicitorRepresented(YES);
        dataAfterSwitchToSoleFo.getApplicant2().setSolicitor(Solicitor.builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        dataAfterSwitchToSoleFo.setFinalOrder(FinalOrder.builder()
            .doesApplicant1IntendToSwitchToSole(YES)
            .dateFinalOrderSubmitted(LocalDateTime.now())
            .build());

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(callbackRequest(dataAfterSwitchToSoleFo, SWITCH_TO_SOLE_FO_OFFLINE)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(eq(TEST_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verify(notificationService)
            .sendEmail(eq(TEST_SOLICITOR_EMAIL), eq(SOLICITOR_PARTNER_HAS_SWITCHED_TO_SOLE_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong());
        verifyNoMoreInteractions(notificationService);
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
    }
}
