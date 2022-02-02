package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordPageBuilder;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordManuallyHandled;
import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class ManuallyHandleExceptionRecord implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    public static final String UPDATE_MANUALLY = "updateManually";

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        new ExceptionRecordPageBuilder(configBuilder
            .event(UPDATE_MANUALLY)
            .forStateTransition(ScannedRecordReceived, ScannedRecordManuallyHandled)
            .name("Manually handle record")
            .description("Manually handle record")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SYSTEMUPDATE))
            .page("updateManually")
            .pageLabel("Correspondence")
            .mandatory(ExceptionRecord::getScannedDocuments)
            .mandatory(ExceptionRecord::getScanOCRData);
    }
}
