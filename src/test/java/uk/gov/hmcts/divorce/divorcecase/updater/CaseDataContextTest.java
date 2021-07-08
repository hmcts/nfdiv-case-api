package uk.gov.hmcts.divorce.divorcecase.updater;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static java.lang.System.identityHashCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class CaseDataContextTest {

    @Test
    void shouldReturnNewInstanceOfCaseData() {

        final CaseData caseData = CaseData.builder().build();
        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .build();

        final CaseData copyOfCaseData = caseDataContext.copyOfCaseData();

        assertThat(identityHashCode(copyOfCaseData), is(not(identityHashCode(caseData))));
    }

    @Test
    void shouldReturnHandlerContextWithCaseData() {

        final CaseData caseData1 = CaseData.builder().build();
        final CaseData caseData2 = CaseData.builder().build();
        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData1)
            .build();

        final CaseDataContext result = caseDataContext.handlerContextWith(caseData2);

        assertThat(identityHashCode(result), is(identityHashCode(caseDataContext)));
        assertThat(identityHashCode(result.getCaseData()), is(identityHashCode(caseData2)));
    }
}
