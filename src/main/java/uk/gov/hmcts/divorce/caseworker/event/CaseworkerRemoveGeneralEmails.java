package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CaseworkerRemoveGeneralEmails implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_REMOVE_GENERAL_EMAILS = "caseworker-remove-general-emails";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_GENERAL_EMAILS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Remove general emails")
            .description("Remove general emails")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER))
            .page("removeGeneralEmails")
            .pageLabel("Remove general emails")
            .optional(CaseData::getGeneralEmails)
            .done();
    }
}
