package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerUpdateDateSubmitted implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_DATE_SUBMITTED = "caseworker-update-date-submitted";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_DATE_SUBMITTED)
            .forStates(POST_SUBMISSION_STATES)
            .name("Update Date Submitted")
            .description("Update Date Submitted")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                SUPER_USER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("updateDateSubmitted")
            .complex(CaseData::getApplication)
                .mandatory(Application::getDateSubmitted)
                .done()
            .done();
    }
}
