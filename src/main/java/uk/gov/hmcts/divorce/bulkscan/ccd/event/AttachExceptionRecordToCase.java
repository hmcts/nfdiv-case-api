package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordPageBuilder;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordAttachedToCase;
import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class AttachExceptionRecordToCase implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    public static final String ATTACH_TO_EXISTING_CASE = "attachToExistingCase";

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        new ExceptionRecordPageBuilder(configBuilder
            .event(ATTACH_TO_EXISTING_CASE)
            .forStateTransition(ScannedRecordReceived, ScannedRecordAttachedToCase)
            .name("Attach record to existing case")
            .description("Attach record to existing case")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SYSTEMUPDATE))
            .page("attachToExistingCase")
            .pageLabel("Correspondence")
            .readonlyNoSummary(ExceptionRecord::getShowEnvelopeCaseReference,"envelopeCaseReference=\"ALWAYS_HIDE\"")
            .readonlyNoSummary(ExceptionRecord::getShowEnvelopeLegacyCaseReference,"envelopeLegacyCaseReference=\"ALWAYS_HIDE\"")
            .readonly(ExceptionRecord::getEnvelopeCaseReference,"showEnvelopeCaseReference=\"Yes\"")
            .readonly(ExceptionRecord::getShowEnvelopeLegacyCaseReference,"showEnvelopeLegacyCaseReference=\"Yes\"")
            .mandatory(ExceptionRecord::getSearchCaseReference)
            .mandatory(ExceptionRecord::getScannedDocuments);
    }
}
