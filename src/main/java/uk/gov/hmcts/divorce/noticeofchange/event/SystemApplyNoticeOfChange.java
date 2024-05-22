package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.NOC_APPROVER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest.acaRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemApplyNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {

    private final  AuthTokenGenerator authTokenGenerator;
    private final  ObjectMapper objectMapper;
    private final  AssignCaseAccessClient assignCaseAccessClient;
    private final  IdamService idamService;
    private final OrganisationClient organisationClient;

    public static final String NOTICE_OF_CHANGE_APPLIED = "notice-of-change-applied";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(NOTICE_OF_CHANGE_APPLIED)
            .forStates(POST_SUBMISSION_STATES)
            .name("Notice Of Change Applied")
            .grant(CREATE_READ_UPDATE, NOC_APPROVER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, CASE_WORKER, SUPER_USER)
            .aboutToStartCallback(this::aboutToStart));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Applying notice of change for case id: {}", details.getId());

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        String s2sToken = authTokenGenerator.generate();

        AboutToStartOrSubmitCallbackResponse response =
            assignCaseAccessClient.applyNoticeOfChange(sysUserToken, s2sToken, acaRequest(details));

        updateChangeOfRepresentation(details.getData(), sysUserToken, s2sToken);

        CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(responseData)
            .state(details.getState())
            .build();
    }

    private void updateChangeOfRepresentation(CaseData caseData, String sysUserToken, String s2sToken) {
        var changeOrganisationRequest = caseData.getChangeOrganisationRequestField();
        var loggedInUserEmail = changeOrganisationRequest.getCreatedBy().toLowerCase();
        var applicant1Solicitor = caseData.getApplicant1().getSolicitor();
        var applicant2Solicitor = caseData.getApplicant2().getSolicitor();
        var organisationId = changeOrganisationRequest.getOrganisationToAdd().getOrganisationId();

        List<ProfessionalUser> organisationUsers =
                Optional.ofNullable(organisationClient.getOrganisationUsers(sysUserToken, s2sToken, organisationId))
                        .map(FindUsersByOrganisationResponse::getUsers)
                        .orElse(Collections.emptyList());

        final var nocRequestingUser = organisationUsers.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(loggedInUserEmail))
                .findFirst()
                .orElseThrow();

        String nocSolicitorOrgName = organisationClient
                .getOrganisationByUserId(sysUserToken, s2sToken, nocRequestingUser.getUserIdentifier())
                .getName();

        if (APPLICANT_1_SOLICITOR.getRole().equals(changeOrganisationRequest.getCaseRoleId().getRole())) {
            updateOrgPolicyAndSolicitorDetails(applicant1Solicitor, nocSolicitorOrgName, nocRequestingUser, loggedInUserEmail);
        } else {
            updateOrgPolicyAndSolicitorDetails(applicant2Solicitor, nocSolicitorOrgName, nocRequestingUser, loggedInUserEmail);
        }
    }

    private static void updateOrgPolicyAndSolicitorDetails(Solicitor applicantSolicitor, String nocSolicitorOrgName,
                                                           ProfessionalUser nocRequestingUser, String loggedInUserEmail) {
        applicantSolicitor.getOrganisationPolicy()
                .getOrganisation().setOrganisationName(nocSolicitorOrgName);
        applicantSolicitor.setName(String.join(" ", nocRequestingUser.getFirstName(), nocRequestingUser.getLastName()));
        applicantSolicitor.setEmail(loggedInUserEmail);
        applicantSolicitor.getOrganisationPolicy().getOrganisation().setOrganisationName(nocSolicitorOrgName);
    }
}
