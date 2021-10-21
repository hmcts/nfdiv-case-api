package uk.gov.hmcts.divorce.common.ccd;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;

public class PageBuilder<T, R extends HasRole, S> {

    private final EventBuilder<T, R, S> eventBuilder;

    public PageBuilder(final EventBuilder<T, R, S> eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public FieldCollectionBuilder<T, S, EventBuilder<T, R, S>> page(final String id) {
        return eventBuilder.fields().page(id);
    }

    public FieldCollectionBuilder<T, S, EventBuilder<T, R, S>> page(
        final String id,
        final MidEvent<T, S> callback) {

        return eventBuilder.fields().page(id, callback);
    }
}
