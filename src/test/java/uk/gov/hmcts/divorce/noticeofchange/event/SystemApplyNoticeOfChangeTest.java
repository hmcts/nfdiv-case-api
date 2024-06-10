package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.DynamicListItem;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.NOTICE_OF_CHANGE_APPLIED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class SystemApplyNoticeOfChangeTest {

    private static final String TEST_ORGANISATION_NAME = "organisation_name";
    private static final String TEST_ORGANISATION_USER_ID = "user_id";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamService idamService;
    @Mock
    private AssignCaseAccessClient assignCaseAccessClient;
    @Mock
    private OrganisationClient organisationClient;

    @Mock
    private User systemUser;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemApplyNoticeOfChange systemApplyNoticeOfChange;

    public void setup() {
        List<ProfessionalUser> professionalUsers = new ArrayList<>();
        professionalUsers.add(ProfessionalUser.builder().email(TEST_SOLICITOR_EMAIL).userIdentifier(TEST_ORGANISATION_USER_ID).build());
        FindUsersByOrganisationResponse findUsersByOrganisationResponse = FindUsersByOrganisationResponse
                .builder().users(professionalUsers).build();

        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder().name(TEST_ORGANISATION_NAME).build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(organisationClient.getOrganisationUsers(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_ORG_ID))
                .thenReturn(findUsersByOrganisationResponse);
        when(organisationClient.getOrganisationByUserId(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_ORGANISATION_USER_ID))
                .thenReturn(organisationsResponse);
    }

    @Test
    void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemApplyNoticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(NOTICE_OF_CHANGE_APPLIED);
    }

    @Test
    void shouldApplyNoticeOfChangeForApplicant1Solicitor() {
        setup();
        CaseData applicant1CaseData = buildCaseDataApplicant1();
        var details =  CaseDetails.<CaseData, State>builder().data(applicant1CaseData).build();
        AcaRequest acaRequest = AcaRequest.acaRequest(details);
        Map<String, Object> expectedData = expectedData(applicant1CaseData);
        when(objectMapper.convertValue(expectedData, CaseData.class)).thenReturn(applicant1CaseData);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse
                .builder().data(expectedData).build();
        when(assignCaseAccessClient.applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest))
                .thenReturn(response);

        systemApplyNoticeOfChange.aboutToStart(details);

        Organisation updatedOrganisation = details.getData().getApplicant1().getSolicitor()
                .getOrganisationPolicy().getOrganisation();

        verify(assignCaseAccessClient).applyNoticeOfChange(
            TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest
        );

        assertEquals(TEST_ORGANISATION_NAME, updatedOrganisation.getOrganisationName());
        assertEquals(TEST_ORG_ID, updatedOrganisation.getOrganisationId());
        assertEquals(TEST_SOLICITOR_EMAIL, details.getData().getApplicant1().getSolicitor().getEmail());
    }

    @Test
    void shouldApplyNoticeOfChangeForApplicant2Solicitor() {
        setup();
        CaseData applicant2CaseData = buildCaseDataApplicant2();
        var details =  CaseDetails.<CaseData, State>builder().data(applicant2CaseData).build();
        AcaRequest acaRequest = AcaRequest.acaRequest(details);
        Map<String, Object> expectedData = expectedData(applicant2CaseData);
        when(objectMapper.convertValue(expectedData, CaseData.class)).thenReturn(applicant2CaseData);

        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse
                .builder().data(expectedData).build();
        when(assignCaseAccessClient.applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest))
                .thenReturn(response);

        systemApplyNoticeOfChange.aboutToStart(details);

        Organisation updatedOrganisation = details.getData().getApplicant2().getSolicitor()
                .getOrganisationPolicy().getOrganisation();

        verify(assignCaseAccessClient).applyNoticeOfChange(
                TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest
        );

        assertEquals(TEST_ORG_NAME, updatedOrganisation.getOrganisationName());
        assertEquals(TEST_ORG_ID, updatedOrganisation.getOrganisationId());
        assertEquals(TEST_SOLICITOR_EMAIL, details.getData().getApplicant2().getSolicitor().getEmail());
    }

    @Test
    void shouldNotApplyNoticeOfChangeWhenErrorsThrown() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        var details =  CaseDetails.<CaseData, State>builder().build();
        AcaRequest acaRequest = AcaRequest.acaRequest(details);

        List<String> errors = List.of("One of the org policies is missing for NoC");
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse
                .builder().errors(errors).build();
        when(assignCaseAccessClient.applyNoticeOfChange(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest))
                .thenReturn(response);

        systemApplyNoticeOfChange.aboutToStart(details);

        verify(assignCaseAccessClient).applyNoticeOfChange(
                TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, acaRequest
        );

        verifyNoInteractions(objectMapper);
    }

    private CaseData buildCaseDataApplicant1() {
        final Applicant applicant1 = TestDataHelper.applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPONESOLICITOR]",
                "APPLICANT_1_SOLICITOR");

        return CaseData.builder()
                .applicationType(SOLE_APPLICATION)
                .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
                .applicant1(applicant1)
                .changeOrganisationRequestField(changeOrganisationRequest)
                .build();
    }

    private CaseData buildCaseDataApplicant2() {
        final Applicant applicant2 = TestDataHelper.respondentWithDigitalSolicitor();
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPTWOSOLICITOR]",
                "APPLICANT_2_SOLICITOR");

        return CaseData.builder()
                .applicationType(SOLE_APPLICATION)
                .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
                .applicant2(applicant2)
                .changeOrganisationRequestField(changeOrganisationRequest)
                .build();
    }

    private static ChangeOrganisationRequest<CaseRoleID> getChangeOrganisationRequestField(String role, String roleLabel) {
        DynamicListItem dynamicListItem = DynamicListItem.builder().label(
                roleLabel).code(role).build();
        List<DynamicListItem> dynamicListItemList = new ArrayList<>();
        dynamicListItemList.add(dynamicListItem);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = ChangeOrganisationRequest.<CaseRoleID>builder().build();
        changeOrganisationRequest.setCaseRoleId(CaseRoleID.builder().value(dynamicListItem).listItems(dynamicListItemList).build());
        changeOrganisationRequest.setCreatedBy(TEST_SOLICITOR_EMAIL);
        changeOrganisationRequest.setOrganisationToAdd(Organisation
                .builder().organisationId(TEST_ORG_ID).organisationName(TEST_ORG_NAME).build());
        changeOrganisationRequest.setOrganisationToRemove(Organisation
                .builder().organisationId(TEST_ORG_ID).organisationName(TEST_ORGANISATION_NAME).build());
        return changeOrganisationRequest;
    }

    private Map<String, Object> expectedData(final CaseData caseData) {

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        return objectMapper.convertValue(caseData, new TypeReference<>() {
        });
    }
}
