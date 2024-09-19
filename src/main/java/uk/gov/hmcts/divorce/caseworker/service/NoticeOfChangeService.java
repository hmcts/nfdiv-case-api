package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorValidationService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private final CcdUpdateService ccdUpdateService;
    private final CcdAccessService ccdAccessService;
    private final SolicitorValidationService solicitorValidationService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;

    public void revokeCaseAccess(Long caseId, Applicant applicant, List<String> roles) {
        log.info("Revoking case access for roles {} for case {}", roles, caseId);

        ccdAccessService.removeUsersWithRole(caseId, roles);

        String orgId = applicant.getSolicitor().getOrganisationPolicy().getOrganisation().getOrganisationId();

        log.info("Resetting supplementary data for org {} on case {}", orgId, caseId);

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        ccdUpdateService.resetOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            sysUserToken,
            s2sToken,
            orgId
        );

    }

    public void changeAccessWithinOrganisation(Solicitor newSolicitor,
                                               List<String> roles,
                                               String solicitorRole,
                                               Long caseId) {
        final String solicitorId = getSolicitorId(caseId, newSolicitor);
        final String orgId = newSolicitor.getOrganisationPolicy().getOrganisation().getOrganisationId();

        log.info("Re-assigning cases access for users in organisation {} on case {}", orgId, caseId);

        ccdAccessService.removeUsersWithRole(caseId, roles);

        if (StringUtils.isNotBlank(solicitorId)) {
            ccdAccessService.addRoleToCase(
                solicitorId,
                caseId,
                orgId,
                UserRole.fromString(solicitorRole)
            );
        }

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        log.info("Updating orgsAssignedUsers supplementary data for org {} on case {}", orgId, caseId);

        ccdUpdateService.setOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            sysUserToken,
            s2sToken,
            orgId,
            "1"
        );
    }

    public void applyNocDecisionAndGrantAccessToNewSol(Long caseId,
                                                       Applicant applicant,
                                                       Applicant applicantBefore,
                                                       List<String> roles,
                                                       String solicitorRole) {
        log.info("Applying Notice of Change Decision and granting access to new sol for case {}", caseId);

        final Solicitor solicitor = applicant.getSolicitor();
        final String solicitorId = getSolicitorId(caseId, solicitor);
        final boolean wasPreviousRepresentationDigital = ccdAccessService.getCaseAssignmentUserRoles(caseId).stream()
            .anyMatch(userRole -> userRole.getCaseRole().equals(solicitorRole));

        if (wasPreviousRepresentationDigital) {
            log.info("Previous solicitor was digital, revoking access for role {} on case {}", solicitorRole, caseId);
            revokeCaseAccess(caseId, applicantBefore, roles);
        }


        if (StringUtils.isBlank(solicitorId)) {
            return;
        }

        grantCaseAccessForNewSol(caseId, applicant, solicitorId, UserRole.fromString(solicitorRole));

        if (solicitor.getAddress() == null) {
            OrganisationsResponse organisationDetails = getSolicitorOrganisationDetails(solicitorId);
            solicitor.setAddressToOrganisationDefault(organisationDetails);
        }
    }

    private void grantCaseAccessForNewSol(Long caseId,
                                          Applicant applicant,
                                          String solicitorId,
                                          UserRole solicitorRole) {

        log.info("Granting case access for new solicitor on case {}", caseId);
        String orgId = applicant.getSolicitor().getOrganisationPolicy().getOrganisation().getOrganisationId();

        ccdAccessService.addRoleToCase(
            solicitorId,
            caseId,
            orgId,
            solicitorRole);

        log.info("Setting orgAssignedusers Supplementary Data to 1 for org {} on case {}", orgId, caseId);

        ccdUpdateService.setOrgAssignedUsersSupplementaryData(
            caseId.toString(),
            idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
            authTokenGenerator.generate(),
            orgId,
            "1"
        );
    }

    private String getSolicitorId(long caseId, Solicitor newSolicitor) {
        String userId = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(newSolicitor.getEmail())) {
            Optional<String> userIdOption = solicitorValidationService.findSolicitorByEmail(newSolicitor.getEmail().toLowerCase(), caseId);
            String orgId = newSolicitor.getOrganisationPolicy().getOrganisation().getOrganisationId();

            if (userIdOption.isEmpty()) {
                throw new NoSuchElementException(String.format("No userId found for user with email %s", newSolicitor.getEmail()));
            }

            if (!solicitorValidationService.isSolicitorInOrganisation(userIdOption.get(), orgId)) {
                throw new IllegalArgumentException(String.format("User is not in specified organisation for case %s", caseId));
            }
            userId = userIdOption.get();
        }

        return userId;
    }

    private OrganisationsResponse getSolicitorOrganisationDetails(String solicitorId) {
        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();
        return organisationClient.getOrganisationByUserId(sysUserToken, s2sToken, solicitorId);
    }
}
