package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorValidationService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangeServiceTest {

    private static final String ORG_ID_TWO = "ORG_ID_TWO";
    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private SolicitorValidationService solicitorValidationService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private OrganisationClient organisationClient;

    @InjectMocks
    private NoticeOfChangeService noticeOfChangeService;

    @Test
    public void shouldRevokeAccessForOrganisation() {
        Applicant applicant = getApplicant(UserRole.APPLICANT_1_SOLICITOR);
        Long caseId = 1234567890L;
        List<String> roles = List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(TEST_SERVICE_AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        noticeOfChangeService.revokeCaseAccess(caseId, applicant, roles);

        verify(ccdAccessService).removeUsersWithRole(caseId, roles);
        verify(ccdUpdateService).resetOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_ORG_ID
        );
    }

    @Test
    public void shouldChangeAccessWithinOrganisation() {
        Long caseId = 1234567890L;
        String userId = "userIdTest";

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(TEST_SERVICE_AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(solicitorValidationService.findSolicitorByEmail(TEST_SOLICITOR_EMAIL, caseId)).thenReturn(Optional.of(userId));
        when(solicitorValidationService.isSolicitorInOrganisation(userId, TEST_ORG_ID)).thenReturn(true);

        Applicant applicant = getApplicant(UserRole.APPLICANT_1_SOLICITOR);
        List<String> roles = List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        noticeOfChangeService.changeAccessWithinOrganisation(applicant.getSolicitor(), roles, APPLICANT_1_SOLICITOR.getRole(), caseId);

        verify(ccdAccessService).removeUsersWithRole(caseId, roles);
        String orgId = applicant.getSolicitor().getOrganisationPolicy().getOrganisation().getOrganisationId();
        verify(ccdAccessService).addRoleToCase(userId, caseId, orgId, APPLICANT_1_SOLICITOR);
        verify(ccdUpdateService).setOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            orgId,
            "1");
    }

    @Test
    public void shouldThrowNoSuchElementExceptionWhenUserNotFound() {
        Applicant applicant = getApplicant(UserRole.APPLICANT_1_SOLICITOR);
        Long caseId = 1234567890L;
        List<String> roles = List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        when(solicitorValidationService.findSolicitorByEmail(TEST_SOLICITOR_EMAIL, caseId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
            NoSuchElementException.class,
            () -> noticeOfChangeService.changeAccessWithinOrganisation(
                applicant.getSolicitor(),
                roles,
                APPLICANT_1_SOLICITOR.getRole(),
                caseId)
        );

        assertEquals(exception.getMessage(), "No userId found for user with email " + TEST_SOLICITOR_EMAIL);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenUserIsNotInSpecifiedOrg() {
        Applicant applicant = getApplicant(UserRole.APPLICANT_1_SOLICITOR);
        Long caseId = 1234567890L;
        List<String> roles = List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        when(solicitorValidationService.findSolicitorByEmail(TEST_SOLICITOR_EMAIL, caseId)).thenReturn(Optional.of(SOLICITOR_USER_ID));
        when(solicitorValidationService.isSolicitorInOrganisation(SOLICITOR_USER_ID, TEST_ORG_ID)).thenReturn(false);

        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> noticeOfChangeService.changeAccessWithinOrganisation(
                applicant.getSolicitor(),
                roles,
                APPLICANT_1_SOLICITOR.getRole(),
                caseId)
        );

        assertEquals(exception.getMessage(), "User is not in specified organisation for case " + caseId);
    }

    @Test
    public void shouldApplyNoticeOfChangeDecisionWhenPreviousRepresentationWasNotDigital() {
        Long caseId = 1234567890L;
        String userId = "userIdTest";
        OrganisationsResponse organisationResponse = OrganisationsResponse.builder()
            .name(TEST_ORG_NAME)
            .contactInformation(List.of(OrganisationContactInformation.builder().addressLine1(TEST_SOLICITOR_ADDRESS).build()))
            .build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(TEST_SERVICE_AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(solicitorValidationService.findSolicitorByEmail(TEST_SOLICITOR_EMAIL, caseId)).thenReturn(Optional.of(userId));
        when(solicitorValidationService.isSolicitorInOrganisation(userId, TEST_ORG_ID)).thenReturn(true);
        when(ccdAccessService.getCaseAssignmentUserRoles(caseId)).thenReturn(List.of(
            CaseAssignmentUserRole.builder()
                .caseRole(APPLICANT_1_SOLICITOR.getRole())
                .build()
        ));
        when(organisationClient.getOrganisationByUserId(TEST_SERVICE_AUTH_TOKEN, SERVICE_AUTHORIZATION, userId))
            .thenReturn(organisationResponse);

        Applicant applicant = getApplicant(UserRole.APPLICANT_2_SOLICITOR);
        Applicant applicantBefore = getApplicant(APPLICANT_2_SOLICITOR);
        applicantBefore.getSolicitor().getOrganisationPolicy().setOrganisation(null);
        List<String> roles = List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());

        noticeOfChangeService.applyNocDecisionAndGrantAccessToNewSol(caseId,
            applicant,
            applicantBefore,
            roles,
            APPLICANT_2_SOLICITOR.getRole());

        verify(ccdAccessService, never()).removeUsersWithRole(any(), any());
        verify(ccdAccessService).addRoleToCase(userId, caseId, TEST_ORG_ID, APPLICANT_2_SOLICITOR);
        verify(ccdUpdateService).setOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_ORG_ID,
            "1");
        assertEquals(applicant.getSolicitor().getAddress(), TEST_SOLICITOR_ADDRESS);
    }

    @Test
    public void shouldApplyNoticeOfChangeDecisionWhenPreviousRepresentationWasDigital() {
        Long caseId = 1234567890L;
        String userId = "userIdTest";
        OrganisationsResponse organisationResponse = OrganisationsResponse.builder()
            .name(TEST_ORG_NAME)
            .contactInformation(List.of(OrganisationContactInformation.builder().addressLine1(TEST_SOLICITOR_ADDRESS).build()))
            .build();

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(TEST_SERVICE_AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(solicitorValidationService.findSolicitorByEmail(TEST_SOLICITOR_EMAIL, caseId)).thenReturn(Optional.of(userId));
        when(solicitorValidationService.isSolicitorInOrganisation(userId, TEST_ORG_ID)).thenReturn(true);
        when(ccdAccessService.getCaseAssignmentUserRoles(caseId)).thenReturn(List.of(
            CaseAssignmentUserRole.builder()
                .caseRole(APPLICANT_2_SOLICITOR.getRole())
                .build()
        ));
        when(organisationClient.getOrganisationByUserId(TEST_SERVICE_AUTH_TOKEN, SERVICE_AUTHORIZATION, userId))
            .thenReturn(organisationResponse);

        Applicant applicant = getApplicant(UserRole.APPLICANT_2_SOLICITOR);
        Applicant applicantBefore = getApplicant(APPLICANT_2_SOLICITOR);
        applicantBefore.getSolicitor().getOrganisationPolicy().getOrganisation().setOrganisationId(ORG_ID_TWO);
        List<String> roles = List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());

        noticeOfChangeService.applyNocDecisionAndGrantAccessToNewSol(caseId,
            applicant,
            applicantBefore,
            roles,
            APPLICANT_2_SOLICITOR.getRole());

        verify(ccdAccessService).removeUsersWithRole(caseId, roles);
        verify(ccdAccessService).addRoleToCase(userId, caseId, TEST_ORG_ID, APPLICANT_2_SOLICITOR);
        verify(ccdUpdateService).resetOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            ORG_ID_TWO
        );
        verify(ccdUpdateService).setOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            TEST_ORG_ID,
            "1");
        assertEquals(applicant.getSolicitor().getAddress(), TEST_SOLICITOR_ADDRESS);
    }

    private static Applicant getApplicant(UserRole caseRole) {
        return Applicant.builder()
            .solicitor(
                Solicitor.builder()
                    .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                        .orgPolicyCaseAssignedRole(caseRole)
                        .organisation(Organisation.builder()
                            .organisationId(TEST_ORG_ID)
                            .build())
                        .build())
                    .email(TEST_SOLICITOR_EMAIL)
                    .build()
            )
            .build();
    }

}
