package uk.gov.hmcts.divorce.bulkscan.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.ExceptionRecord;
import uk.gov.hmcts.divorce.bulkscan.ccd.ExceptionRecordState;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.ccd.event.RejectExceptionRecord.REJECT_RECORD;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createExceptionRecordConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class RejectExceptionRecordTest {

    @InjectMocks
    private RejectExceptionRecord rejectExceptionRecord;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<ExceptionRecord, ExceptionRecordState, UserRole> configBuilder = createExceptionRecordConfigBuilder();

        rejectExceptionRecord.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(REJECT_RECORD);
    }
}
