package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseMatch;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerUpdateInappropriateMatches implements CCDConfig<CaseData, State, UserRole> {

    public static final String UPDATE_INAPPROPRIATE_MATCHES = "caseworker-update-inappropriate-matches";
    private static final EnumSet<State> EVENT_STATES = EnumSet.copyOf(POST_SUBMISSION_STATES);
    public static final String NO_INAPPROPRIATE_MATCHES_ERROR = "No inappropriate matches found";

    static {
        EVENT_STATES.remove(State.Archived);
    }

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(UPDATE_INAPPROPRIATE_MATCHES)
            .forStates(EVENT_STATES)
            .name("Update inappropriate matches")
            .description("Update inappropriate matches")
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
            .page("inappropriatematch")
            .pageLabel("Update inappropriate case matches")
            .readonlyNoSummary(CaseData::getInappropriateCaseMatches)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", UPDATE_INAPPROPRIATE_MATCHES, details.getId());
        List<ListValue<CaseMatch>> inappropriateCaseMatches = details.getData().getInappropriateCaseMatches();
        if (inappropriateCaseMatches == null || inappropriateCaseMatches.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .errors(List.of(NO_INAPPROPRIATE_MATCHES_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
