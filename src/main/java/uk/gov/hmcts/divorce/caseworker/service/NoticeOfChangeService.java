package uk.gov.hmcts.divorce.caseworker.service;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.caseworker.model.NoticeOfChangeRequest;
import uk.gov.hmcts.divorce.common.DecisionRequest;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.ManageCaseAssignmentClient;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorValidationService;
import uk.gov.hmcts.divorce.systemupdate.convert.CallbackResponseConverter;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeOfChangeService {

    private final CcdUpdateService ccdUpdateService;
    private final CcdAccessService ccdAccessService;
    private final SolicitorValidationService solicitorValidationService;
    private final ManageCaseAssignmentClient manageCaseAssignmentClient;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CallbackResponseConverter callbackResponseConverter;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final Clock clock;

    public AboutToStartOrSubmitResponse<CaseData, State> revokeCaseAccessForOrganisation(NoticeOfChangeRequest nocRequest) {

        Set<String> nonSolRolesToRemove = Sets.difference(new HashSet<>(nocRequest.getRoles()), Set.of(nocRequest.getSolicitorRole()));

        Optional<UserRole> roleToRemove = Optional.ofNullable(
            UserRole.fromString(nonSolRolesToRemove.stream().findFirst().orElse(StringUtils.EMPTY))
        );

        if (roleToRemove.isEmpty()) {
            throw new IllegalStateException(String.format("Could not find role to remove for case %s", nocRequest.getDetails().getId()));
        }

        ccdAccessService.removeUsersWithRole(nocRequest.getDetails().getId(), nonSolRolesToRemove.stream().toList());

        setChangeOrganisationRequestOnCaseData(nocRequest, roleToRemove.get());

        String sysUserAuthToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        return applyNoticeOfChangeDecision(sysUserAuthToken, nocRequest.getDetails());
    }

    public void changeAccessWithinOrganisation(Solicitor newSolicitor,
                                               List<String> roles,
                                               String solicitorRole,
                                               long caseId) {

        final String solicitorId = getSolicitorId(caseId, newSolicitor);
        final String orgId = newSolicitor.getOrganisationPolicy().getOrganisation().getOrganisationId();

        ccdAccessService.removeUsersWithRole(caseId, roles);

        if (StringUtils.isNotBlank(solicitorId)) {
            ccdAccessService.addRoleToCase(
                solicitorId,
                caseId,
                orgId,
                UserRole.fromString(solicitorRole)
            );
        }

        //If there's no id found for new solicitor and we reach here, then move to unassigned cases list, else set assigned users to one
        String newOrgAssignedUsersValue = StringUtils.isNotBlank(solicitorId) ? "1" : "0";

        ccdUpdateService.setOrgAssignedUsersSupplementaryData(
            String.valueOf(caseId),
            idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
            authTokenGenerator.generate(),
            orgId,
            newOrgAssignedUsersValue
        );
    }

    public void revokeAccessForSolAndReturnToUnassignedCases(Applicant applicant,
                                                             long caseId,
                                                             List<String> roles) {
        final var orgId = applicant.getSolicitor().getOrganisationPolicy().getOrganisation().getOrganisationId();

        ccdAccessService.removeUsersWithRole(caseId, roles);

        ccdUpdateService.resetOrgAssignedUsersSupplementaryData(
            String.valueOf(caseId),
            idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
            authTokenGenerator.generate(),
            orgId
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> applyNocDecisionAndGrantAccessToNewSol(NoticeOfChangeRequest nocRequest) {

        final String solicitorId = getSolicitorId(nocRequest.getDetails().getId(), nocRequest.getApplicant().getSolicitor());

        Set<String> nonSolRolesToUpdate = Sets.difference(new HashSet<>(nocRequest.getRoles()), Set.of(nocRequest.getSolicitorRole()));
        Optional<UserRole> solicitorRole = Optional.ofNullable(
            UserRole.fromString(nonSolRolesToUpdate.stream().findFirst().orElse(StringUtils.EMPTY))
        );

        if (solicitorRole.isEmpty()) {
            throw new IllegalStateException(String.format("Could not find role to update for case %s", nocRequest.getDetails().getId()));
        }

        //revoke creator (and citizen) roles if present, else MCA will have a fit and reject the apply-decision call
        ccdAccessService.removeUsersWithRole(nocRequest.getDetails().getId(), nonSolRolesToUpdate.stream().toList());

        setChangeOrganisationRequestOnCaseData(nocRequest, solicitorRole.get());

        AboutToStartOrSubmitResponse<CaseData, State> response = applyNoticeOfChangeDecision(
            idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
            nocRequest.getDetails()
        );

        if (StringUtils.isNotBlank(solicitorId)) {
            grantCaseAccessForNewSol(nocRequest, solicitorId, solicitorRole.get());
        }

        return response;

    }

    private void setChangeOrganisationRequestOnCaseData(NoticeOfChangeRequest nocRequest,
                                                        UserRole solicitorRole) {

        final var organisationToAdd = Optional.ofNullable(nocRequest.getApplicant().getSolicitor().getOrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .orElse(null);

        final var organisationToRemove = Optional.ofNullable(nocRequest.getApplicantBefore().getSolicitor().getOrganisationPolicy())
                .map(OrganisationPolicy::getOrganisation)
                .orElse(null);

        ChangeOrganisationRequest<UserRole> changeOrganisationRequest = generateChangeOrganisationRequest(
            organisationToAdd,
            organisationToRemove,
            solicitorRole
        );

        nocRequest.getDetails().getData().getNoticeOfChange().setChangeOrganisationRequest(changeOrganisationRequest);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> applyNoticeOfChangeDecision(String authToken,
                                                                                      CaseDetails<CaseData, State> details) {

        AboutToStartOrSubmitCallbackResponse response = manageCaseAssignmentClient.applyDecision(authToken,
            authTokenGenerator.generate(),
            DecisionRequest.decisionRequest(
                caseDetailsConverter.convertToReformModelFromCaseDetails(details)
            )
        );


        return callbackResponseConverter.convertResponseFromCcdModel(response);
    }

    private ChangeOrganisationRequest<UserRole> generateChangeOrganisationRequest(Organisation organisationToAdd,
                                                                                  Organisation organisationToRemove,
                                                                                  UserRole caseRoleId) {
        return ChangeOrganisationRequest.<UserRole>builder()
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .organisationToAdd(organisationToAdd)
            .organisationToRemove(organisationToRemove)
            .caseRoleId(caseRoleId)
            .requestTimestamp(LocalDateTime.now(clock))
            .build();
    }

    private void grantCaseAccessForNewSol(NoticeOfChangeRequest nocRequest,
                                          String solicitorId,
                                          UserRole solicitorRole) {
        String orgId = nocRequest.getApplicant().getSolicitor().getOrganisationPolicy().getOrganisation().getOrganisationId();

        ccdAccessService.addRoleToCase(
            solicitorId,
            nocRequest.getDetails().getId(),
            orgId,
            solicitorRole);

        ccdUpdateService.incrementOrgAssignedUsersSupplementaryData(
            String.valueOf(nocRequest.getDetails().getId()),
            idamService.retrieveSystemUpdateUserDetails().getAuthToken(),
            authTokenGenerator.generate(),
            orgId
        );
    }

    private String getSolicitorId(long caseId, Solicitor newSolicitor) {
        String userId = StringUtils.EMPTY;

        if (StringUtils.isNotBlank(newSolicitor.getEmail())) {
            Optional<String> userIdOption = solicitorValidationService.findSolicitorByEmail(newSolicitor.getEmail(), caseId);
            String orgId = newSolicitor.getOrganisationPolicy().getOrganisation().getOrganisationId();

            if (userIdOption.isEmpty()) {
                throw new NoSuchElementException("Generic Error");
            }

            if (!solicitorValidationService.isSolicitorInOrganisation(userIdOption.get(), orgId)) {
                throw new IllegalArgumentException(String.format("User is not in specified organisation for case %s", caseId));
            }
            userId = userIdOption.get();
        }

        return userId;
    }
}
