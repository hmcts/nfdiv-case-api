package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.bulkscan.data.ExceptionRecord;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordCaseCreated;
import static uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState.ScannedRecordManuallyHandled;
import static uk.gov.hmcts.divorce.bulkscan.ccd.event.OfflineDocumentVerified.CASEWORKER_OFFLINE_DOCUMENT_VERIFIED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createExceptionRecordConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class OfflineDocumentVerifiedTest {

    @InjectMocks
    private OfflineDocumentVerified offlineDocumentVerified;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder = createExceptionRecordConfigBuilder();

        offlineDocumentVerified.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED);
    }

    @Test
    void shouldSetStateToUserValueProvided() {
        final CaseDetails<ExceptionRecord, ExceptionRecordState> details = new CaseDetails<>();
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .stateToTransitionTo("ScannedRecordManuallyHandled")
            .build();
        details.setData(exceptionRecord);

        AboutToStartOrSubmitResponse<ExceptionRecord, ExceptionRecordState> response =
            offlineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getData().getStateToTransitionTo()).isBlank();
        assertThat(response.getState()).isEqualTo(ScannedRecordManuallyHandled);
    }

    @Test
    void shouldHandleInvalidStateProvided() {
        final CaseDetails<ExceptionRecord, ExceptionRecordState> details = new CaseDetails<>();
        ExceptionRecord exceptionRecord = ExceptionRecord.builder()
            .stateToTransitionTo("InvalidState")
            .build();
        details.setData(exceptionRecord);
        details.setState(ScannedRecordCaseCreated);

        AboutToStartOrSubmitResponse<ExceptionRecord, ExceptionRecordState> response =
            offlineDocumentVerified.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(ScannedRecordCaseCreated);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("State entered is not a valid Exception Record State");
    }
}
