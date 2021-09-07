package uk.gov.hmcts.divorce.divorcecase.tab;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.andNotShowForState;

class TabShowConditionTest {

    @Test
    void shouldReturnAosTabShowConditionForCaseStates() {
        assertThat((andNotShowForState(Draft, AwaitingHWFDecision, AwaitingPayment, Submitted, AwaitingDocuments)))
            .isEqualTo("[STATE]!=\"Draft\" AND [STATE]!=\"AwaitingHWFDecision\" AND [STATE]!=\"AwaitingPayment\" "
                + "AND [STATE]!=\"Submitted\" AND [STATE]!=\"AwaitingDocuments\"");
    }
}