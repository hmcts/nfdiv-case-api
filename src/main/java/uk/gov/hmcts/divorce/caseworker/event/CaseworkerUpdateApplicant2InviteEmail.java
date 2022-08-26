package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerUpdateApplicant2InviteEmail implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_APP_2_INVITE_EMAIL = "caseworker-update-app2-invite-email";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var updateCaseStateEnabled = Boolean.parseBoolean(System.getenv().get("CITIZEN_UPDATE_CASE_STATE_ENABLED"));
        if (updateCaseStateEnabled) {
            new uk.gov.hmcts.divorce.common.ccd.PageBuilder(configBuilder
                .event(CASEWORKER_UPDATE_APP_2_INVITE_EMAIL)
                .forStates(POST_SUBMISSION_STATES)
                .name("Update applicant2 invite email")
                .description("Update applicant 2 invite email")
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER, CASE_WORKER))
                .page("updateApp2InviteEmail")
                .pageLabel("Update applicant 2 invite email")
                .complex(CaseData::getCaseInvite)
                    .optional(CaseInvite::applicant2InviteEmailAddress)
                .done();
        }
    }
}
