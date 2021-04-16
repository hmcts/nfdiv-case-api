package uk.gov.hmcts.divorce.util;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.util.ReflectionUtils.findField;

@ExtendWith(MockitoExtension.class)
class CaseDataUpdaterChainFactoryTest {

    @InjectMocks
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Test
    void shouldCreateNewInstanceOfCaseDataUpdaterChainWithListOfCaseDataUpdaters() throws IllegalAccessException {

        final CaseDataUpdater caseDataUpdater1 = mock(CaseDataUpdater.class);
        final CaseDataUpdater caseDataUpdater2 = mock(CaseDataUpdater.class);
        final LinkedList<CaseDataUpdater> caseDataUpdaters = new LinkedList<>();
        caseDataUpdaters.add(caseDataUpdater1);
        caseDataUpdaters.add(caseDataUpdater2);

        final CaseDataUpdaterChain caseDataUpdaterChain = caseDataUpdaterChainFactory.createWith(caseDataUpdaters);

        final Deque<CaseDataUpdater> caseDataUpdaterDeque = getCaseDataUpdatersFrom(caseDataUpdaterChain);

        assertThat(caseDataUpdaterDeque.pop(), CoreMatchers.is(caseDataUpdater1));
        assertThat(caseDataUpdaterDeque.pop(), CoreMatchers.is(caseDataUpdater2));
        assertThat(caseDataUpdaterDeque.isEmpty(), is(true));
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private Deque<CaseDataUpdater> getCaseDataUpdatersFrom(final CaseDataUpdaterChain caseDataUpdaterChain)
        throws IllegalAccessException {

        final Field field = findField(CaseDataUpdaterChain.class, "caseDataUpdaters", Deque.class);
        field.setAccessible(true);
        return (Deque<CaseDataUpdater>) field.get(caseDataUpdaterChain);
    }
}
