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
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.DraftAos.DRAFT_AOS;
import static uk.gov.hmcts.divorce.common.event.DraftAos.DRAFT_AOS_ALREADY_SUBMITTED_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class DraftAosTest {

    @Mock
    private AddMiniApplicationLink addMiniApplicationLink;

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
        expectedCaseData.setApplication(Application.builder().issueDate(LocalDate.of(2022, 1, 1)).build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updateCaseDetails = new CaseDetails<>();
        caseDetails.setData(expectedCaseData);
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
        assertThat(response.getData().getAcknowledgementOfService().getAosIsDrafted()).isEqualTo(YES);
    }

    @Test
    void shouldNotChangeTheStateAndReturnCaseDataOnAboutToSubmit() {
        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(expectedCaseData);
        caseDetails.setState(AwaitingConditionalOrder);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingConditionalOrder);
        assertThat(response.getData().getAcknowledgementOfService().getAosIsDrafted()).isEqualTo(YES);
    }

    @Test
    void shouldThrowErrorAndReturnCaseDataOnAboutToStartIfAosHasAlreadyBeenDrafted() {
        final CaseData caseData = CaseData.builder().build();
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .confirmReadPetition(YES)
            .build();
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setApplication(Application.builder().issueDate(LocalDate.of(2022, 1, 1)).build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AosDrafted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(caseData);
        assertThat(response.getErrors())
            .containsExactly(
                "The Acknowledgement Of Service has already been drafted.");
    }

    @Test
    void shouldThrowErrorAndReturnCaseDataOnAboutToStartIfApplicationHasNotBeenIssuedYet() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(caseData);
        assertThat(response.getErrors())
            .containsExactly(
                "You cannot draft the AoS until the case has been issued. Please wait for the case to be issued.");
    }

    @Test
    void shouldThrowErrorAndReturnCaseDataOnAboutToStartIfAosHasAlreadyBeenSubmitted() {
        final CaseData caseData = CaseData.builder().build();
        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .dateAosSubmitted(LocalDateTime.of(2021, 10, 26, 10, 0, 0))
            .build();
        caseData.setAcknowledgementOfService(acknowledgementOfService);
        caseData.setApplication(Application.builder().issueDate(LocalDate.of(2022, 1, 1)).build());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AosDrafted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftAos.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(caseData);
        assertThat(response.getErrors())
            .containsExactly(DRAFT_AOS_ALREADY_SUBMITTED_ERROR);
    }
}
