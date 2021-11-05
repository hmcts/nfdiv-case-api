package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;

@Service
@Slf4j
public class CcdAccessService {
    @Autowired
    private CaseUserApi caseUserApi;

    @Autowired
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void addApplicant1SolicitorRole(String solicitorIdamToken, Long caseId, String orgId) {
        User solicitorUser = idamService.retrieveUser(solicitorIdamToken);
        User systemUpdateUser = idamService.retrieveSystemUpdateUserDetails();

        Set<String> caseRoles = Set.of(APPLICANT_1_SOLICITOR.getRole());

        String solicitorUserId = solicitorUser.getUserDetails().getId();

        log.info("Adding roles {} to case Id {} and user Id {}",
            caseRoles,
            caseId,
            solicitorUserId
        );

        String idamToken = systemUpdateUser.getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        caseAssignmentApi.removeCaseUserRoles(
            idamToken,
            s2sToken,
            CaseAssignmentUserRolesRequest
                .builder()
                .caseAssignmentUserRolesWithOrganisation(
                    List.of(
                        CaseAssignmentUserRoleWithOrganisation.builder()
                            .caseDataId(caseId.toString())
                            .organisationId(orgId)
                            .caseRole(CREATOR.getRole())
                            .userId(solicitorUserId)
                            .build()
                    )
                )
                .build()
        );

        caseAssignmentApi.addCaseUserRoles(
            idamToken,
            s2sToken,
            CaseAssignmentUserRolesRequest
                .builder()
                .caseAssignmentUserRolesWithOrganisation(
                    List.of(
                        CaseAssignmentUserRoleWithOrganisation.builder()
                            .caseDataId(caseId.toString())
                            .organisationId(orgId)
                            .caseRole(APPLICANT_1_SOLICITOR.getRole())
                            .userId(solicitorUserId)
                            .build()
                    )
                )
                .build()
        );

        log.info("Successfully added the applicant's solicitor roles to case Id {} ", caseId);
    }

    public void linkRespondentToApplication(String caseworkerUserToken, Long caseId, String applicant2UserId) {
        User caseworkerUser = idamService.retrieveUser(caseworkerUserToken);
        Set<String> caseRoles = Set.of(APPLICANT_2.getRole());

        caseUserApi.updateCaseRolesForUser(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            String.valueOf(caseId),
            applicant2UserId,
            new CaseUser(applicant2UserId, caseRoles)
        );

        log.info("Successfully linked applicant 2 to case Id {} ", caseId);
    }

    public void unlinkUserFromApplication(String caseworkerUserToken, Long caseId, String userToRemoveId) {
        User caseworkerUser = idamService.retrieveUser(caseworkerUserToken);

        caseUserApi.updateCaseRolesForUser(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            String.valueOf(caseId),
            userToRemoveId,
            new CaseUser(userToRemoveId, Collections.emptySet())
        );

        log.info("Successfully unlinked applicant from case Id {} ", caseId);
    }
}
