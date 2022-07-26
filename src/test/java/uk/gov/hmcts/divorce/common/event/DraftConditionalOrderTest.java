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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddLastAlternativeServiceDocumentLink;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.DraftConditionalOrder.DRAFT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class DraftConditionalOrderTest {

    @Mock
    private AddMiniApplicationLink addMiniApplicationLink;

    @Mock
    private AddLastAlternativeServiceDocumentLink addLastAlternativeServiceDocumentLink;

    @InjectMocks
    private DraftConditionalOrder draftConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        draftConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(DRAFT_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSetStateOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.AwaitingConditionalOrder).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ConditionalOrderDrafted);
    }

    @Test
    void shouldSetIsDraftedForApplicant1OnAboutToSubmit() {

        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.AwaitingConditionalOrder).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted())
            .isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldCallAddMiniApplicationAndReturnCaseDataOnAboutToStart() {

        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updateCaseDetails = new CaseDetails<>();
        updateCaseDetails.setData(expectedCaseData);

        when(addMiniApplicationLink.apply(caseDetails)).thenReturn(caseDetails);
        when(addLastAlternativeServiceDocumentLink.apply(caseDetails)).thenReturn(updateCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(expectedCaseData);

        verify(addMiniApplicationLink).apply(caseDetails);
        verify(addLastAlternativeServiceDocumentLink).apply(caseDetails);
    }
}
