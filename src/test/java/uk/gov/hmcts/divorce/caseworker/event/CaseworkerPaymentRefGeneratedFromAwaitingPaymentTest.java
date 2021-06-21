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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPaymentRefGeneratedFromAwaitingPayment.CASEWORKER_PAYMENT_REF_GENERATED_AWAITING_PAYMENT;

@ExtendWith(MockitoExtension.class)
public class CaseworkerPaymentRefGeneratedFromAwaitingPaymentTest {

    @InjectMocks
    private CaseworkerPaymentRefGeneratedFromAwaitingPayment caseworkerPaymentRefGeneratedFromAwaitingPayment;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerPaymentRefGeneratedFromAwaitingPayment.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_PAYMENT_REF_GENERATED_AWAITING_PAYMENT);
    }
}
