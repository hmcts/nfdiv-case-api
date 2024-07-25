package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
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
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.DynamicListItem;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.citizen.notification.NocCitizenToSolsNotifications;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.NOTICE_OF_CHANGE_APPLIED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.start;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.stopAndReset;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.stubForNoCApplyDecisionEndpoint;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.stubForNoCApplyDecisionEndpointEndpointForFailure;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    ManageCaseAssignmentWireMock.PropertiesInitializer.class
})
public class SystemApplyNoticeOfChangeIT {

    private static final String TEST_ORGANISATION_ID = "HB12345";
    private static final String TEST_ORGANISATION_ID_REMOVE = "AB4567";
    private static final String TEST_ORGANISATION_NAME = "NFD solicitors org";
    private static final String TEST_ORGANISATION_NAME_REMOVE = "Private solicitors Ltd";
    private static final String USER_IDENTIFIER = "123";
    private static final String SOLICITOR_NAME = "test_first_name test_last_name";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private IdamService idamService;

    @MockBean
    private User user;

    @MockBean
    private OrganisationClient organisationClient;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private NocCitizenToSolsNotifications nocCitizenToSolsNotifications;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        start();
    }

    @AfterAll
    static void tearDown() {
        stopAndReset();
    }

    @Test
    void shouldApplyNoticeOfChangeDecision() throws Exception {

        setMockClock(clock);

        List<ProfessionalUser> professionalUsers = new ArrayList<>();

        professionalUsers.add(ProfessionalUser.builder().email(TEST_SOLICITOR_EMAIL)
                .firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).userIdentifier(USER_IDENTIFIER).build());

        OrganisationsResponse organisationResponse = OrganisationsResponse.builder()
                .organisationIdentifier(USER_IDENTIFIER)
                .name(TEST_ORGANISATION_NAME)
                .contactInformation(List.of(
                    OrganisationContactInformation.builder()
                        .addressLine1(TEST_SOLICITOR_ADDRESS)
                        .build()
                ))
                .build();
        FindUsersByOrganisationResponse findUsersByOrganisationResponse =
                FindUsersByOrganisationResponse.builder().users(professionalUsers)
                        .organisationIdentifier(TEST_ORGANISATION_ID).build();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(organisationClient.getOrganisationUsers(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_ORGANISATION_ID))
                .thenReturn(findUsersByOrganisationResponse);
        when(organisationClient.getOrganisationByUserId(TEST_SERVICE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN, USER_IDENTIFIER)).thenReturn(organisationResponse);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequestField = getChangeOrganisationRequestField();
        Solicitor app1Solicitor =
                Solicitor.builder().organisationPolicy(OrganisationPolicy.<UserRole>builder()
                                .organisation(Organisation.builder()
                                        .organisationName(TEST_ORGANISATION_NAME)
                                        .organisationId(TEST_ORGANISATION_ID)
                                        .build())
                                .build())
                        .email(TEST_SOLICITOR_EMAIL)
                        .name(String.join(" ", TEST_FIRST_NAME, TEST_LAST_NAME))
                        .build();

        final CaseData caseData = CaseData.builder().changeOrganisationRequestField(changeOrganisationRequestField)
                .applicant1(Applicant.builder().solicitor(app1Solicitor).build()).build();

        AcaRequest acaRequest = acaRequestBody(caseData);
        stubForNoCApplyDecisionEndpoint(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest);

        Solicitor app1OldSolicitor =
                Solicitor.builder().organisationPolicy(OrganisationPolicy.<UserRole>builder()
                                .organisation(Organisation.builder()
                                        .organisationName(TEST_ORGANISATION_NAME)
                                        .organisationId(TEST_ORGANISATION_ID)
                                        .build())
                                .build())
                        .email("randomEmail@solicitor.com")
                        .name("Test name")
                        .build();

        final CaseData caseDataWithOldSolicitor = CaseData.builder().changeOrganisationRequestField(changeOrganisationRequestField)
                .applicant1(Applicant.builder().solicitor(app1OldSolicitor).build()).build();

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .header(AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .content(objectMapper.writeValueAsString(callbackRequest(
                                caseDataWithOldSolicitor, NOTICE_OF_CHANGE_APPLIED, State.Holding.toString())))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(
                        status().isOk()
                ).andExpect(jsonPath("$.data.applicant1SolicitorName")
                        .value(TEST_SOLICITOR_NAME))
                .andExpect(jsonPath("$.data.applicant1SolicitorEmail")
                        .value(TEST_SOLICITOR_EMAIL))
                .andExpect(jsonPath("$.data.applicant1SolicitorAddress")
                        .value(TEST_SOLICITOR_ADDRESS))
                .andExpect(jsonPath("$.data.applicant1SolicitorOrganisationPolicy.Organisation.OrganisationID")
                        .value(TEST_ORGANISATION_ID))
                .andExpect(jsonPath("$.data.applicant1SolicitorOrganisationPolicy.Organisation.OrganisationName")
                        .value(TEST_ORGANISATION_NAME));
    }

    @Test
    void shouldThrowErrorWhenApplyNoticeOfChange() throws Exception {

        List<ProfessionalUser> professionalUsers = new ArrayList<>();

        professionalUsers.add(ProfessionalUser.builder().email(TEST_SOLICITOR_EMAIL)
                .firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).userIdentifier(USER_IDENTIFIER).build());

        OrganisationsResponse organisationResponse = OrganisationsResponse.builder()
                .organisationIdentifier(USER_IDENTIFIER)
                .name(TEST_ORGANISATION_NAME)
            .build();
        FindUsersByOrganisationResponse findUsersByOrganisationResponse =
                FindUsersByOrganisationResponse.builder().users(professionalUsers)
                        .organisationIdentifier(TEST_ORGANISATION_ID).build();

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(organisationClient.getOrganisationUsers(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_ORGANISATION_ID))
                .thenReturn(findUsersByOrganisationResponse);
        when(organisationClient.getOrganisationByUserId(TEST_SERVICE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN, USER_IDENTIFIER)).thenReturn(organisationResponse);

        Solicitor app1Solicitor =
                Solicitor.builder().organisationPolicy(OrganisationPolicy.<UserRole>builder()
                                .organisation(Organisation.builder()
                                        .organisationName(TEST_ORGANISATION_NAME_REMOVE)
                                        .organisationId(TEST_ORGANISATION_ID_REMOVE)
                                        .build())
                                .build())
                        .build();
        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequestField = getChangeOrganisationRequestField();

        final CaseData caseData = CaseData.builder().changeOrganisationRequestField(changeOrganisationRequestField)
                .applicant1(Applicant.builder().solicitor(app1Solicitor).build()).build();

        AcaRequest acaRequest = acaRequestBody(caseData);
        stubForNoCApplyDecisionEndpointEndpointForFailure(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .header(AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                callbackRequest(caseData, NOTICE_OF_CHANGE_APPLIED, State.Holding.toString())))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
            );
    }

    private static AcaRequest acaRequestBody(CaseData caseData) {
        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = uk.gov.hmcts.ccd.sdk.api.CaseDetails.<CaseData, State>builder()
                .data(caseData)
                .state(State.Holding)
                .id(TEST_CASE_ID)
                .caseTypeId(getCaseType())
                .build();
        return AcaRequest.builder().caseDetails(caseDetails).build();
    }

    private static ChangeOrganisationRequest<CaseRoleID> getChangeOrganisationRequestField() {
        DynamicListItem dynamicListItem = DynamicListItem.builder().label(
                "APPLICANT_1_SOLICITOR").code("[APPONESOLICITOR]").build();
        List<DynamicListItem> dynamicListItemList = new ArrayList<>();
        dynamicListItemList.add(dynamicListItem);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = ChangeOrganisationRequest.<CaseRoleID>builder().build();
        changeOrganisationRequest.setCaseRoleId(CaseRoleID.builder().value(dynamicListItem).listItems(dynamicListItemList).build());
        changeOrganisationRequest.setCreatedBy(TEST_SOLICITOR_EMAIL);
        changeOrganisationRequest.setOrganisationToAdd(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID).organisationName(TEST_ORGANISATION_NAME).build());
        changeOrganisationRequest.setOrganisationToRemove(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID_REMOVE).organisationName(TEST_ORGANISATION_NAME_REMOVE).build());
        changeOrganisationRequest.setApprovalStatus(ChangeOrganisationApprovalStatus.APPROVED);

        return changeOrganisationRequest;
    }
}
