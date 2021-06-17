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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerHwfApplicationAcceptedFromAwaitingPayment.CASEWORKER_HWF_APPLICATION_ACCEPTED_FROM_AWAITING_PAYMENT;

@ExtendWith(MockitoExtension.class)
public class CaseworkerHwfApplicationAcceptedFromAwaitingPaymentTest {

    @InjectMocks
    private CaseworkerHwfApplicationAcceptedFromAwaitingPayment caseworkerHwfApplicationAcceptedFromAwaitingPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerHwfApplicationAcceptedFromAwaitingPayment.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_HWF_APPLICATION_ACCEPTED_FROM_AWAITING_PAYMENT);
    }
}
