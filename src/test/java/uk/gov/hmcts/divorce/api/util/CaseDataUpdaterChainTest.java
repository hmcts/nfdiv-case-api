package uk.gov.hmcts.divorce.api.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CaseDataUpdaterChainTest {

    @Test
    void shouldProcessAllCaseDataUpdatersAndReturnCaseDataContext() {

        final CaseDataContext caseDataContext1 = mock(CaseDataContext.class);
        final CaseDataContext caseDataContext2 = mock(CaseDataContext.class);
        final CaseDataContext initial = mock(CaseDataContext.class);
        final CaseDataContext expected = mock(CaseDataContext.class);

        final CaseDataUpdater caseDataUpdater1 = mock(CaseDataUpdater.class);
        final CaseDataUpdater caseDataUpdater2 = mock(CaseDataUpdater.class);
        final LinkedList<CaseDataUpdater> caseDataUpdaters = new LinkedList<>();
        caseDataUpdaters.add(caseDataUpdater1);
        caseDataUpdaters.add(caseDataUpdater2);

        final CaseDataUpdaterChain caseDataUpdaterChain = new CaseDataUpdaterChain(caseDataUpdaters);

        when(caseDataUpdater1.updateCaseData(initial, caseDataUpdaterChain)).thenReturn(caseDataContext1);
        when(caseDataUpdater2.updateCaseData(caseDataContext1, caseDataUpdaterChain)).thenReturn(caseDataContext2);

        final CaseDataContext result1 = caseDataUpdaterChain.processNext(initial);
        final CaseDataContext result2 = caseDataUpdaterChain.processNext(caseDataContext1);
        final CaseDataContext result3 = caseDataUpdaterChain.processNext(expected);

        assertThat(result1, is(caseDataContext1));
        assertThat(result2, is(caseDataContext2));
        assertThat(result3, is(expected));

        verify(caseDataUpdater1).updateCaseData(initial, caseDataUpdaterChain);
        verify(caseDataUpdater2).updateCaseData(caseDataContext1, caseDataUpdaterChain);

        verifyNoMoreInteractions(caseDataUpdater1, caseDataUpdater2);
    }
}
