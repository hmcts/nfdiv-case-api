package uk.gov.hmcts.divorce.noticeofchange.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.ORGANISATION_CASE_ACCESS_ADMINISTRATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest.acaRequest;

@Slf4j
@Component
public class SystemRequestNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private AssignCaseAccessClient assignCaseAccessClient;

    @Autowired
    private IdamService idamService;

    public static final String NOTICE_OF_CHANGE_REQUESTED = "notice-of-change-requested";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(NOTICE_OF_CHANGE_REQUESTED)
            .forStates(POST_SUBMISSION_STATES)
            .name("Notice Of Change Requested")
            .grant(CREATE_READ_UPDATE, ORGANISATION_CASE_ACCESS_ADMINISTRATOR)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, CASE_WORKER, SUPER_USER)
            .submittedCallback(this::submitted))
            .page("nocRequest")
            .complex(CaseData::getChangeOrganisationRequestField)
                .complex(ChangeOrganisationRequest::getOrganisationToAdd)
                    .optional(Organisation::getOrganisationId)
                    .optional(Organisation::getOrganisationName)
                .done()
                .complex(ChangeOrganisationRequest::getOrganisationToRemove)
                    .optional(Organisation::getOrganisationId)
                    .optional(Organisation::getOrganisationName)
                .done()
                    .optional(ChangeOrganisationRequest::getRequestTimestamp)
                    .optional(ChangeOrganisationRequest::getCaseRoleId)
                .optional(
                    ChangeOrganisationRequest::getApprovalStatus,
                    NEVER_SHOW,
                    ChangeOrganisationApprovalStatus.APPROVED
                )
            .done();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("Notice of change requested for case id: {}", details.getId());

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        assignCaseAccessClient.checkNocApproval(sysUserToken, s2sToken, acaRequest(details));

        return SubmittedCallbackResponse.builder().build();
    }
}
