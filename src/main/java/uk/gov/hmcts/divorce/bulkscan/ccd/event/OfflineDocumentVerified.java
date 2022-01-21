package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordPageBuilder;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.data.ExceptionRecord;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.apache.commons.lang3.EnumUtils.isValidEnum;
import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class OfflineDocumentVerified implements CCDConfig<ExceptionRecord, ExceptionRecordState, UserRole> {

    public static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED = "caseworker-offline-document-verified";

    @Override
    public void configure(final ConfigBuilder<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder) {
        new ExceptionRecordPageBuilder(configBuilder
            .event(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)
            .initialState(OfflineDocumentReceived)
            .name("Offline Document Verified")
            .description("Offline Document Verified")
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SUPER_USER))
            .page("Update case state")
            .pageLabel("Update case state")
            .mandatory(ExceptionRecord::getStateToTransitionTo);
    }

    public AboutToStartOrSubmitResponse<ExceptionRecord, ExceptionRecordState> aboutToSubmit(
        CaseDetails<ExceptionRecord, ExceptionRecordState> details,
        CaseDetails<ExceptionRecord, ExceptionRecordState> beforeDetails
    ) {

        ExceptionRecord data = details.getData();
        if (isValidEnum(ExceptionRecordState.class, data.getStateToTransitionTo())) {
            ExceptionRecordState state = ExceptionRecordState.valueOf(data.getStateToTransitionTo());
            data.setStateToTransitionTo("");

            return AboutToStartOrSubmitResponse.<ExceptionRecord, ExceptionRecordState>builder()
                .data(data)
                .state(state)
                .build();
        }

        return AboutToStartOrSubmitResponse.<ExceptionRecord, ExceptionRecordState>builder()
            .state(details.getState())
            .errors(List.of("State entered is not a valid Exception Record State"))
            .build();
    }
}
