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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.divorce.testutil.IdamWireMock;
import uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemRequestNoticeOfChange.NOTICE_OF_CHANGE_REQUESTED;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.start;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.stopAndReset;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.stubForCheckNocApprovalEndpoint;
import static uk.gov.hmcts.divorce.testutil.ManageCaseAssignmentWireMock.stubForCheckNocApprovalEndpointForFailure;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ContextConfiguration(initializers = {
    ManageCaseAssignmentWireMock.PropertiesInitializer.class,
})
public class SystemRequestNoticeOfChangeIT {

    private static final String TEST_ORGANISATION_ID = "HB12345";
    private static final String TEST_ORGANISATION_ID_REMOVE = "AB4567";
    private static final String TEST_ORGANISATION_NAME = "NFD solicitors org";
    private static final String TEST_ORGANISATION_NAME_REMOVE = "Private solicitors Ltd";

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
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @BeforeAll
    static void setUp() {
        start();
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        stopAndReset();
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldProgressThroughValidationWhenCaseIsSuitableForNoc() throws Exception {
        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .applicant1(Applicant.builder().offline(YesOrNo.YES).build())
            .build();

        var response = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(
                    caseData, NOTICE_OF_CHANGE_REQUESTED, State.Holding.toString())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void shouldNotProgressThroughValidationWhenCaseIsUnsuitableForNoc() throws Exception {

        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .applicant1(Applicant.builder().offline(YesOrNo.YES).build())
            .build();

        var response = mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .header(AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(
                    caseData, NOTICE_OF_CHANGE_REQUESTED, State.Holding.toString())))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldRequestNoticeOfChange() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequestField = getChangeOrganisationRequestField(
        );
        changeOrganisationRequestField.setApprovalStatus(ChangeOrganisationApprovalStatus.APPROVED);

        Solicitor app1Solicitor =
                Solicitor.builder().organisationPolicy(OrganisationPolicy.<UserRole>builder()
                                .organisation(Organisation.builder()
                                        .organisationName(TEST_ORGANISATION_NAME_REMOVE)
                                        .organisationId(TEST_ORGANISATION_ID_REMOVE)
                                        .build())
                                .build())
                        .build();
        final CaseData caseData = CaseData.builder().changeOrganisationRequestField(changeOrganisationRequestField)
                .applicant1(Applicant.builder().solicitor(app1Solicitor).build()).build();

        AcaRequest acaRequest = acaRequestBody(caseData);
        stubForCheckNocApprovalEndpoint(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest);

        String response = mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .header(AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .content(objectMapper.writeValueAsString(callbackRequest(
                                caseData, NOTICE_OF_CHANGE_REQUESTED, State.Holding.toString())))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThatJson(response)
                .isEqualTo("""
                        {
                            "confirmation_header": "Approval Applied",
                            "confirmation_body": "Approval Applied"
                        }
                        """);
    }


    @Test
    void shouldThrowErrorWhenRequestNoticeOfChange() throws Exception {

        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(user.getAuthToken()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        Solicitor app1Solicitor =
                Solicitor.builder().organisationPolicy(OrganisationPolicy.<UserRole>builder()
                                .organisation(Organisation.builder()
                                        .organisationName(TEST_ORGANISATION_NAME_REMOVE)
                                        .organisationId(TEST_ORGANISATION_ID_REMOVE)
                                        .build())
                                .build())
                        .build();
        final CaseData caseData = CaseData.builder()
                .applicant1(Applicant.builder().solicitor(app1Solicitor).build()).build();

        AcaRequest acaRequest = acaRequestBody(caseData);
        stubForCheckNocApprovalEndpointForFailure(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest);

        mockMvc.perform(MockMvcRequestBuilders.post(SUBMITTED_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .header(AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                callbackRequest(caseData, NOTICE_OF_CHANGE_REQUESTED, State.Holding.toString())))
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(
                        status().isForbidden()
                ).andExpect(
                        result -> assertThat(result.getResolvedException()).isExactlyInstanceOf(FeignException.Forbidden.class)
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
        return changeOrganisationRequest;
    }
}
