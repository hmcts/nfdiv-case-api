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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerResolveWelshTranslation.CASEWORKER_RESOLVE_WELSH_TRANSLATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerResolveWelshTranslationTest {
    @InjectMocks
    private CaseworkerResolveWelshTranslation caseworkerResolveWelshTranslation;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerResolveWelshTranslation.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RESOLVE_WELSH_TRANSLATION);
    }

    @Test
    void shouldAddEventWelshTranslationResolvedToEventHistoryAndMoveStateToPreviousStateWhenStateIsWelshTranslationRequested() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(WelshTranslationRequested);
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .welshPreviousState(Submitted)
                .build())
            .build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResolveWelshTranslation.aboutToSubmit(details, null);

        assertThat(response.getState()).isEqualTo(Submitted);
    }

    @Test
    void shouldAddEventWelshTranslationResolvedToEventHistoryAndMoveStateToPreviousStateWhenStateIsWelshTranslationReview() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(WelshTranslationReview);
        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .welshPreviousState(Submitted)
                .build())
            .build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResolveWelshTranslation.aboutToSubmit(details, null);

        assertThat(response.getState()).isEqualTo(Submitted);
    }
}
