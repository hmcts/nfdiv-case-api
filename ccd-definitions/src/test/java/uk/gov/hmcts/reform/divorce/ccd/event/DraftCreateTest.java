package uk.gov.hmcts.reform.divorce.ccd.event;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.types.Event;
import uk.gov.hmcts.reform.divorce.ccd.framework.StubConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;

class DraftCreateTest {

    private final StubConfigBuilder<CaseData, State, UserRole> stubConfigBuilder = new StubConfigBuilder<>();
    private final DraftCreate draftCreate = new DraftCreate();

    @Test
    public void shouldAddDraftCreateEvent() {

        draftCreate.buildWith(stubConfigBuilder);

        final Map<StubConfigBuilder.EventState, List<Event.EventBuilder<CaseData, UserRole, State>>> events = stubConfigBuilder.getEvents();
        assertThat(events.size(), is(1));

        final List<Event.EventBuilder<CaseData, UserRole, State>> draftEvents = events.get(new StubConfigBuilder.EventState("", Draft.toString()));
        assertThat(draftEvents.size(), is(1));
        final Event<CaseData, UserRole, State> event = draftEvents.get(0).build();

        assertThat(event.getEventID(), is("draftCreate"));
        assertThat(event.getId(), is("draftCreate"));
        assertThat(event.getName(), is("Create draft case"));
        assertThat(event.getDescription(), is("Apply for a divorce or dissolution"));
        assertThat(event.getDisplayOrder(), is(1));
    }
}