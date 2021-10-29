package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemUpdateCaseTest {

    @InjectMocks
    private SystemUpdateCase systemUpdateCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        systemUpdateCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_UPDATE_BULK_CASE);
    }
}
