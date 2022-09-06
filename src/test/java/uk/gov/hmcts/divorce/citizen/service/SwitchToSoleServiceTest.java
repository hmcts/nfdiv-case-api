package uk.gov.hmcts.divorce.citizen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder.D84WhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class SwitchToSoleServiceTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private SwitchToSoleService switchToSoleService;

    @Test
    void shouldSwitchUserDataIfApplicant2TriggeredD84SwitchToSole() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());

        final Applicant applicant1BeforeSwitch = caseData.getApplicant1();
        final Applicant applicant2BeforeSwitch = caseData.getApplicant2();

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getApplicant1()).isEqualTo(applicant2BeforeSwitch);
        assertThat(caseData.getApplicant2()).isEqualTo(applicant1BeforeSwitch);
    }

    @Test
    void shouldSetDivorceWhoIfNewApplicant2GenderIsNotNull() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getApplication().getDivorceWho()).isNotNull();
        assertThat(caseData.getApplication().getDivorceWho()).isEqualTo(WIFE);
    }

    @Test
    void shouldSetDivorceWhoToNullIfNewApplicant2GenderIsNull() {
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        caseData.getApplicant1().setGender(null);

        switchToSoleService.switchApplicantData(caseData);

        assertThat(caseData.getApplication().getDivorceWho()).isNull();
    }

    @Test
    void shouldSwitchUserRolesIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = 1L;

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        final UserDetails userDetails = UserDetails.builder().id(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", CREATOR))
            .thenReturn(getCaseAssignmentRequest("2", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("1", UserRole.APPLICANT_2));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", CREATOR))
            .thenReturn(getCaseAssignmentRequest("1", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("2", UserRole.APPLICANT_2));

        switchToSoleService.switchUserRoles(caseId);

        verify(caseAssignmentApi)
            .getUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                List.of(String.valueOf(caseId))
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", CREATOR)
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", UserRole.APPLICANT_2)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", CREATOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", UserRole.APPLICANT_2)
            );
        verifyNoMoreInteractions(caseAssignmentApi);
    }

    private CaseAssignmentUserRolesRequest getCaseAssignmentRequest(String userId, UserRole role) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(getCaseAssignmentUserRole(role.getRole(), userId))
            ).build();
    }

    private CaseAssignmentUserRoleWithOrganisation getCaseAssignmentUserRole(String role, String userId) {
        return CaseAssignmentUserRoleWithOrganisation.builder()
            .organisationId(null)
            .caseDataId(String.valueOf(1L))
            .caseRole(role)
            .userId(userId)
            .build();
    }
}
