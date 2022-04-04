package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.DraftAos.DRAFT_AOS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class DraftAosTest {

    @Mock
    private AddMiniApplicationLink addMiniApplicationLink;

    @Mock
    private Clock clock;

    @InjectMocks
    private DraftAos draftAos;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        draftAos.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(DRAFT_AOS);
    }

    @Test
    void shouldCallAddMiniApplicationAndReturnCaseDataOnAboutToStart() {

        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updateCaseDetails = new CaseDetails<>();
        updateCaseDetails.setData(expectedCaseData);

        when(addMiniApplicationLink.apply(caseDetails)).thenReturn(updateCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(expectedCaseData);

        verify(addMiniApplicationLink).apply(caseDetails);
    }

    @Test
    void shouldChangeTheStateAndReturnCaseDataOnAboutToSubmit() {
        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(expectedCaseData);
        caseDetails.setState(AwaitingAos);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AosDrafted);
    }

    @Test
    void shouldNotChangeTheStateAndReturnCaseDataOnAboutToSubmit() {
        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(expectedCaseData);
        caseDetails.setState(AwaitingConditionalOrder);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingConditionalOrder);
    }

    @Test
    void shouldThrowErrorAndReturnCaseDataOnAboutToSubmit() {
        setMockClock(clock);
        final CaseData caseData = CaseData.builder().build();
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .dateAosSubmitted(LocalDateTime.now(clock))
            .build();
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingConditionalOrder);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isSameAs(caseData);
        assertThat(response.getErrors())
            .containsExactly(
                "The Acknowledgement Of Service has already been submitted.");
    }
}
