package uk.gov.hmcts.divorce.bulkaction.ccd;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.model.UserRole;

public class BulkActionPageBuilder {

    private final EventBuilder<BulkActionCaseData, UserRole, BulkActionState> eventBuilder;

    public BulkActionPageBuilder(final EventBuilder<BulkActionCaseData, UserRole, BulkActionState> eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public FieldCollectionBuilder<BulkActionCaseData, BulkActionState, EventBuilder<BulkActionCaseData, UserRole, BulkActionState>> page(final String id) {
        return eventBuilder.fields().page(id);
    }

    public FieldCollectionBuilder<BulkActionCaseData, BulkActionState, EventBuilder<BulkActionCaseData, UserRole, BulkActionState>> page(
        final String id,
        final MidEvent<BulkActionCaseData, BulkActionState> callback) {

        return eventBuilder.fields().page(id, callback);
    }
}
