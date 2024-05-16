package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationUser;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import static com.microsoft.applicationinsights.web.dependencies.http.HttpHeaders.AUTHORIZATION;
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

    private final OrganisationClient organisationClient;
    private final  AuthTokenGenerator authTokenGenerator;
    private final  ObjectMapper objectMapper;
    private final  AssignCaseAccessClient assignCaseAccessClient;
    private final  IdamService idamService;
  //  private final HttpServletRequest httpServletRequest;

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

    //    User aacToken = idamService.retrieveUser(httpServletRequest.getHeader(AUTHORIZATION));
        String s2sToken = authTokenGenerator.generate();

//        String loggedInUserEmail = details.getData().getChangeOrganisationRequestField().getCreatedBy();
//       OrganisationUser user = organisationClient.findUserByEmail(sysUserToken, s2sToken, loggedInUserEmail);
//
//        String nocSolicitorOrgName = organisationClient
//                .getOrganisationByUserId(sysUserToken, s2sToken, user.getUserIdentifier())
//                .getName();
//
//        var organisationId = details.getData().getChangeOrganisationRequestField().getOrganisationToAdd().getOrganisationId();
//
//        final var loggedInUserDetails = Optional.ofNullable(organisationClient.getOrganisationUsers(sysUserToken, s2sToken, organisationId))
//                .map(FindUsersByOrganisationResponse::getUsers)
//                .orElse(Collections.emptyList());

        AboutToStartOrSubmitCallbackResponse response =
            assignCaseAccessClient.applyNoticeOfChange(sysUserToken, s2sToken, acaRequest(details));

        CaseData responseData = objectMapper.convertValue(response.getData(), CaseData.class);

        if (APPLICANT_1_SOLICITOR.getRole().equals(details.getData().getChangeOrganisationRequestField().getCaseRoleId().getRole())) {
         //   responseData.getApplicant1().getSolicitor().getOrganisationPolicy().getOrganisation().setOrganisationName(nocSolicitorOrgName);
        } else {
           // responseData.getApplicant2().getSolicitor().getOrganisationPolicy().getOrganisation().setOrganisationName(nocSolicitorOrgName);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(responseData)
            .state(details.getState())
            .build();
    }
}
