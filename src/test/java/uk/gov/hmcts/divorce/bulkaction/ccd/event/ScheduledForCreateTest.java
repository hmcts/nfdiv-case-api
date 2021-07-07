package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ScheduledForCreateTest {

    @InjectMocks
    private ScheduledForCreate scheduledForCreate;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<BulkActionState> stateSet = EnumSet.allOf(BulkActionState.class);
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder =
            new ConfigBuilderImpl<>(BulkActionCaseData.class, stateSet);

        scheduledForCreate.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(ScheduledForCreate.SCHEDULE_CREATE);
    }
}
