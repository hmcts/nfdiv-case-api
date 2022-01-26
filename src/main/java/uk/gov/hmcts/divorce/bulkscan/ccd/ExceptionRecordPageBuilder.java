package uk.gov.hmcts.divorce.bulkscan.ccd;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class ExceptionRecordPageBuilder {

    private final EventBuilder<ExceptionRecord, UserRole, ExceptionRecordState> eventBuilder;

    public ExceptionRecordPageBuilder(final EventBuilder<ExceptionRecord, UserRole, ExceptionRecordState> eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public FieldCollectionBuilder<
        ExceptionRecord,
        ExceptionRecordState,
        EventBuilder<ExceptionRecord, UserRole, ExceptionRecordState>
        > page(final String id) {
        return eventBuilder.fields().page(id);
    }

    public FieldCollectionBuilder<
        ExceptionRecord, ExceptionRecordState,
        EventBuilder<ExceptionRecord, UserRole, ExceptionRecordState>
        > page(
        final String id,
        final MidEvent<ExceptionRecord, ExceptionRecordState> callback) {

        return eventBuilder.fields().page(id, callback);
    }
}
