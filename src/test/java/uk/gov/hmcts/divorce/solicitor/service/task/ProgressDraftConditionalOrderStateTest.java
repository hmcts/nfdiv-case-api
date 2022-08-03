package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ProgressDraftConditionalOrderStateTest {

    @InjectMocks
    private ProgressDraftConditionalOrderState progressDraftConditionalOrderState;

    @Test
    void shouldSetStateToConditionalOrderDraftedIfFirstInTimeToDraftConditionalOrder() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingConditionalOrder);

        final CaseDetails<CaseData, State> result = progressDraftConditionalOrderState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(ConditionalOrderDrafted);
    }

    @Test
    void shouldNotSetStateIfAnyApplicantHasAlreadySubmittedAConditionalOrder() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(ConditionalOrderPending);

        final CaseDetails<CaseData, State> result = progressDraftConditionalOrderState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(ConditionalOrderPending);
    }
}
