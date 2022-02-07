package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.fromString;

@Service
@Slf4j
public class CcdAccessService {

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void addApplicant1SolicitorRole(String solicitorIdamToken, Long caseId, String orgId) {
        User solicitorUser = idamService.retrieveUser(solicitorIdamToken);
        User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        String solicitorUserId = solicitorUser.getUserDetails().getId();

        log.info("Adding roles {} to case Id {} and user Id {}",
            APPLICANT_1_SOLICITOR,
            caseId,
            solicitorUserId
        );

        String idamToken = systemUpdateUser.getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        caseAssignmentApi.removeCaseUserRoles(
            idamToken,
            s2sToken,
            getCaseAssignmentRequest(caseId, solicitorUserId, orgId, CREATOR)
        );

        caseAssignmentApi.addCaseUserRoles(
            idamToken,
            s2sToken,
            getCaseAssignmentRequest(caseId, solicitorUserId, orgId, APPLICANT_1_SOLICITOR)
        );

        log.info("Successfully added the applicant's solicitor roles to case Id {} ", caseId);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void addRoleToCase(String userId, Long caseId, String orgId, UserRole role) {
        log.info("Adding roles {} to case Id {} and user Id {}", role, caseId, userId);

        User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();
        String idamToken = systemUpdateUser.getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        caseAssignmentApi.addCaseUserRoles(
            idamToken,
            s2sToken,
            getCaseAssignmentRequest(caseId, userId, orgId, role)
        );

        log.info("Successfully added the role to case Id {} ", caseId);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void linkRespondentToApplication(String caseworkerUserToken, Long caseId, String applicant2UserId) {
        User caseworkerUser = idamService.retrieveUser(caseworkerUserToken);

        caseAssignmentApi.addCaseUserRoles(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            getCaseAssignmentRequest(caseId, applicant2UserId, null, APPLICANT_2)
        );

        log.info("Successfully linked applicant 2 to case Id {} ", caseId);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public void unlinkUserFromApplication(String caseworkerUserToken, Long caseId, String userToRemoveId) {
        User caseworkerUser = idamService.retrieveUser(caseworkerUserToken);

        caseAssignmentApi.removeCaseUserRoles(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            getCaseAssignmentRequest(caseId, userToRemoveId, null, APPLICANT_2)
        );

        log.info("Successfully unlinked applicant from case Id {} ", caseId);
    }

    @Retryable(value = {FeignException.class, RuntimeException.class})
    public boolean isApplicant1(String userToken, Long caseId) {
        log.info("Retrieving roles for user on case {}", caseId);
        User user = idamService.retrieveUser(userToken);
        List<String> userRoles =
            caseAssignmentApi.getUserRoles(
                userToken,
                authTokenGenerator.generate(),
                List.of(String.valueOf(caseId)),
                List.of(user.getUserDetails().getId())
            )
            .getCaseAssignmentUserRoles()
            .stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .collect(Collectors.toList());
        return userRoles.contains(CREATOR.getRole()) || userRoles.contains(APPLICANT_1_SOLICITOR.getRole());
    }

    private CaseAssignmentUserRolesRequest getCaseAssignmentRequest(Long caseId, String userId, String orgId, UserRole role) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(
                    CaseAssignmentUserRoleWithOrganisation.builder()
                        .organisationId(orgId)
                        .caseDataId(String.valueOf(caseId))
                        .caseRole(role.getRole())
                        .userId(userId)
                        .build()
                )
            )
            .build();
    }

    public void removeUsersWithRole(String auth, Long caseId, List<String> roles) {
        final var s2sToken = authTokenGenerator.generate();
        final var response = caseAssignmentApi.getUserRoles(auth, s2sToken, List.of(caseId.toString()));

        response.getCaseAssignmentUserRoles()
            .stream()
            .filter(assignment -> roles.contains(assignment.getCaseRole()))
            .map(a -> getCaseAssignmentRequest(caseId, a.getUserId(), null, fromString(a.getCaseRole())))
            .forEach(request -> caseAssignmentApi.removeCaseUserRoles(auth, s2sToken, request));
    }
}
