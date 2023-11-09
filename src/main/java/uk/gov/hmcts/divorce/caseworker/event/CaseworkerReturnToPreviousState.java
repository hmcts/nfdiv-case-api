package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_ISSUE_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PRE_RETURN_TO_PREVIOUS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerReturnToPreviousState implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_RETURN_TO_PREVIOUS_STATE = "caseworker-return-to-previous-state";
    private static final String INVALID_STATE_ERROR
        = "You cannot move this case into a pre-submission state. Select another state before continuing.";
    private static final String CASE_MUST_BE_ISSUED_ERROR
        = "You cannot move this case into a post-issue state as it has not been issued";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_RETURN_TO_PREVIOUS_STATE)
            .forStates(PRE_RETURN_TO_PREVIOUS_STATES)
            .name("Return to previous state")
            .description("Return to previous state")
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
            .page("returnToPreviousState", this::midEvent)
            .pageLabel("Return to previous state")
            .complex(CaseData::getApplication)
                .readonly(Application::getCurrentState)
                .mandatoryWithLabel(Application::getStateToTransitionApplicationTo, "State to transfer case to")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        List<String> validationErrors = new ArrayList<>();

        State state = caseData.getApplication().getStateToTransitionApplicationTo();
        if (!PRE_RETURN_TO_PREVIOUS_STATES.contains(state)) {
            validationErrors.add(INVALID_STATE_ERROR);
        }
        if (POST_ISSUE_STATES.contains(state) && caseData.getApplication().getIssueDate() == null) {
            validationErrors.add(CASE_MUST_BE_ISSUED_ERROR);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(validationErrors)
            .build();

    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Caseworker return to previous state about to start callback invoked for Case Id: {}", details.getId());

        CaseData caseData = details.getData();
        caseData.getApplication().setCurrentState(details.getState());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Caseworker return to previous state about to submit callback invoked for Case Id: {}", details.getId());

        CaseData caseData = details.getData();
        State state = caseData.getApplication().getStateToTransitionApplicationTo();

        return AboutToStartOrSubmitResponse.<CaseData, State>
                builder()
            .state(state)
            .data(caseData)
            .build();
    }
}
