package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerReturnToPreviousState.CASEWORKER_RETURN_TO_PREVIOUS_STATE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant1Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerReturnToPreviousStateTest {

    private static final String INVALID_STATE_ERROR
        = "You cannot move this case into a pre-submission state. Select another state before continuing.";
    private static final String CASE_MUST_BE_ISSUED_ERROR
        = "You cannot move this case into a post-issue state as it has not been issued";

    @InjectMocks
    private CaseworkerReturnToPreviousState caseworkerReturnToPreviousState;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerReturnToPreviousState.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RETURN_TO_PREVIOUS_STATE);
    }

    @Test
    void shouldSetCurrentStateInAboutToStartCallback() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder().build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setState(Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReturnToPreviousState.aboutToStart(details);

        assertThat(response.getData().getApplication().getCurrentState()).isEqualTo(Submitted);
    }

    @Test
    void shouldMoveToSelectedStateWhenSelectedByCaseworker() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(AosDrafted)
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReturnToPreviousState.aboutToSubmit(details, null);

        assertThat(response.getState()).isEqualTo(AosDrafted);
    }

    @Test
    void shouldReturnValidationErrorWhenPreSubmissionStateSelectedByCaseworker() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(AwaitingApplicant1Response)
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReturnToPreviousState.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(INVALID_STATE_ERROR);
    }

    @Test
    void shouldReturnValidationErrorWhenMovingToPostIssuedStateWhenNoIssueDate() {
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .stateToTransitionApplicationTo(Holding)
                .build()
            ).build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerReturnToPreviousState.midEvent(details, null);

        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo(CASE_MUST_BE_ISSUED_ERROR);
    }
}
