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
import uk.gov.hmcts.divorce.common.service.task.SetLatestBailiffApplicationStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressDraftConditionalOrderState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.DraftJointConditionalOrder.DRAFT_JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class DraftJointConditionalOrderTest {

    @Mock
    private AddMiniApplicationLink addMiniApplicationLink;

    @Mock
    private ProgressDraftConditionalOrderState progressDraftConditionalOrderState;

    @Mock
    private SetLatestBailiffApplicationStatus setLatestBailiffApplicationStatus;

    @InjectMocks
    private DraftJointConditionalOrder draftJointConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        draftJointConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(DRAFT_JOINT_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSetIsDraftedForApplicant2OnAboutToSubmit() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftJointConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted())
            .isEqualTo(YES);
    }

    @Test
    void shouldSetApplyForConditionalOrderOnJointIfNoSelected() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .applyForConditionalOrder(NO)
                    .applyForConditionalOrderIfNo(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftJointConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getApplyForConditionalOrder())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getApplyForConditionalOrderIfNo())
            .isNull();
    }

    @Test
    void shouldCallProgressDraftConditionalOrderStateOnAboutToSubmit() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        draftJointConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        verify(progressDraftConditionalOrderState).apply(caseDetails);
    }

    @Test
    void shouldCallAddMiniApplicationAndReturnCaseDataOnAboutToStart() {

        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updateCaseDetails = new CaseDetails<>();
        updateCaseDetails.setData(expectedCaseData);

        when(addMiniApplicationLink.apply(caseDetails)).thenReturn(caseDetails);
        when(setLatestBailiffApplicationStatus.apply(caseDetails)).thenReturn(updateCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftJointConditionalOrder.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(expectedCaseData);

        verify(addMiniApplicationLink).apply(caseDetails);
        verify(setLatestBailiffApplicationStatus).apply(caseDetails);
    }
}
