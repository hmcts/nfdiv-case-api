package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.testutil.DocAssemblyWireMock;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorSwitchToSoleCo.APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO;
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
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
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
public class Applicant2SolicitorSwitchToSoleCoIT {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO_RESPONSE =
        "classpath:solicitor-applicant2-switch-to-sole-co-response.json";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private CcdAccessService ccdAccessService;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private CaseAssignmentApi caseAssignmentApi;

    @MockitoBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        DocAssemblyWireMock.start();
        IdamWireMock.start();
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
    }

    @Test
    void shouldSwitchApplicationTypeToSoleSwitchUsersDataAndGenerateConditionalOrderAnswers()
        throws Exception {

        final String app1SolicitorEmail = "app1solicitor@test.com";
        final String app2SolicitorEmail = "app2solicitor@test.com";

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .firmName("App1 Sol Firm")
                .email(app1SolicitorEmail)
                .build()
        );
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .firmName("App2 Sol Firm")
                .email(app2SolicitorEmail)
                .build()
        );

        stubForIdamDetails(TEST_AUTHORIZATION_TOKEN, CASEWORKER_USER_ID, CASEWORKER_ROLE);

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPTWOSOLICITOR]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[APPONESOLICITOR]").build()
            ))
            .build();
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(
            "Bearer " + TEST_SYSTEM_AUTHORISATION_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            List.of(String.valueOf(TEST_CASE_ID))
        )).thenReturn(caseRolesResponse);
        when(idamService.retrieveSystemUpdateUserDetails())
            .thenReturn(new User("Bearer " + TEST_SYSTEM_AUTHORISATION_TOKEN, UserInfo.builder().build()));

        stubForIdamDetails(TEST_SYSTEM_AUTHORISATION_TOKEN, SYSTEM_USER_USER_ID, SYSTEM_USER_ROLE);
        stubForIdamToken(TEST_SYSTEM_AUTHORISATION_TOKEN);
        stubForDocAssemblyWith("5cd725e8-f053-4493-9cbe-bb69d1905ae3", "FL-NFD-GOR-ENG-Conditional_Order_Answers.docx");

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    callbackRequest(data, APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO, "ConditionalOrderPending")))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .when(IGNORING_EXTRA_FIELDS)
            .when(IGNORING_ARRAY_ORDER)
            .isEqualTo(json(expectedResponse(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO_RESPONSE)));
    }

    @Test
    void shouldNotifyApplicant1SolicitorAndApplicant2Solicitor()
        throws Exception {

        final String app1SolicitorEmail = "app1solicitor@test.com";
        final String app2SolicitorEmail = "app2solicitor@test.com";

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App1 Sol")
                .firmName("App1 Sol Firm")
                .email(app1SolicitorEmail)
                .build()
        );
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .firmName("App2 Sol Firm")
                .email(app2SolicitorEmail)
                .build()
        );

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    callbackRequest(data, APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(app1SolicitorEmail),
                eq(SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong());
        verify(notificationService)
            .sendEmail(
                eq(app2SolicitorEmail),
                eq(SOLICITOR_OTHER_PARTY_MADE_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotifyApplicant1SolicitorAndApplicant2()
        throws Exception {

        final String app1SolicitorEmail = "app1solicitor@test.com";

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LOCAL_DATE);

        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .firmName("App2 Sol Firm")
                .email(app1SolicitorEmail)
                .build()
        );
        data.getApplicant2().setEmail(TEST_USER_EMAIL);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    callbackRequest(data, APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(app1SolicitorEmail),
                eq(SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong());
        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(PARTNER_SWITCHED_TO_SOLE_CO),
                anyMap(),
                eq(ENGLISH),
                anyLong());
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldNotifyApplicant1SolicitorAndApplicant2Welsh()
        throws Exception {

        final String app1SolicitorEmail = "app1solicitor@test.com";

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LOCAL_DATE);

        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(
            Solicitor.builder()
                .name("App2 Sol")
                .firmName("App2 Sol Firm")
                .email(app1SolicitorEmail)
                .build()
        );
        data.getApplicant2().setEmail(TEST_USER_EMAIL);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);

        mockMvc.perform(post(SUBMITTED_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(OBJECT_MAPPER.writeValueAsString(
                    callbackRequest(data, APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(notificationService)
            .sendEmail(
                eq(app1SolicitorEmail),
                eq(SOLICITOR_SOLE_APPLICATION_FOR_CONDITIONAL_ORDER),
                anyMap(),
                eq(ENGLISH),
                anyLong());
        verify(notificationService)
            .sendEmail(
                eq(TEST_USER_EMAIL),
                eq(PARTNER_SWITCHED_TO_SOLE_CO),
                anyMap(),
                eq(WELSH),
                anyLong());
        verifyNoMoreInteractions(notificationService);
    }
}
