package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordPageBuilder;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
public class CreateExceptionRecord implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    public static final String CREATE_EXCEPTION = "createException";

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        new ExceptionRecordPageBuilder(configBuilder
            .event(CREATE_EXCEPTION)
            .initialState(ScannedRecordReceived)
            .name("Create an exception record")
            .description("Create an exception record")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SYSTEMUPDATE))
            .page("createException")
            .pageLabel("Correspondence")
            .readonly(ExceptionRecord::getEnvelopeLabel)
            .optional(ExceptionRecord::getJourneyClassification)
            .optional(ExceptionRecord::getPoBox)
            .optional(ExceptionRecord::getPoBoxJurisdiction)
            .optional(ExceptionRecord::getDeliveryDate)
            .optional(ExceptionRecord::getOpeningDate)
            .optional(ExceptionRecord::getScannedDocuments)
            .optional(ExceptionRecord::getScanOCRData)
            .optional(ExceptionRecord::getFormType)
            .optional(ExceptionRecord::getEnvelopeCaseReference)
            .optional(ExceptionRecord::getEnvelopeLegacyCaseReference)
            .optional(ExceptionRecord::getShowEnvelopeCaseReference)
            .optional(ExceptionRecord::getShowEnvelopeLegacyCaseReference);
    }
}
