package uk.gov.hmcts.divorce.divorcecase.tab;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.notShowForState;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.showForState;

class TabShowConditionTest {

    @Test
    void shouldOnlyShowTabIfCaseStateIsNotAnyOfGivenStates() {
        assertThat((notShowForState(Draft, AwaitingHWFDecision, AwaitingPayment, Submitted, AwaitingDocuments)))
            .isEqualTo("[STATE]!=\"Draft\" AND [STATE]!=\"AwaitingHWFDecision\" AND [STATE]!=\"AwaitingPayment\" "
                + "AND [STATE]!=\"Submitted\" AND [STATE]!=\"AwaitingDocuments\"");
    }

    @Test
    void shouldOnlyShowTabIfCaseStateAnyOfGivenStates() {
        assertThat((showForState(Draft, AwaitingHWFDecision, AwaitingPayment, Submitted, AwaitingDocuments)))
            .isEqualTo("[STATE]=\"Draft\" OR [STATE]=\"AwaitingHWFDecision\" OR [STATE]=\"AwaitingPayment\" "
                + "OR [STATE]=\"Submitted\" OR [STATE]=\"AwaitingDocuments\"");
    }
}