package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRefund.CASEWORKER_REFUND;

@ExtendWith(MockitoExtension.class)
class CaseworkerRefundTest {
    @InjectMocks
    private CaseworkerRefund caseworkerRefund;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = EnumSet.allOf(State.class);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerRefund.configure(configBuilder);
        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_REFUND);
    }
}
