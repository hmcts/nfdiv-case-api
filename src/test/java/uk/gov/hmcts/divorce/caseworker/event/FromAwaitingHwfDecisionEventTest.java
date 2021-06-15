package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.AwaitingApplicant.AWAITING_DOCS_FROM_AWAITING_HWF;
import static uk.gov.hmcts.divorce.caseworker.event.HwfApplicationAccepted.HWF_APPLICATION_ACCEPTED_FROM_AWAITING_HWF_DECISION;
import static uk.gov.hmcts.divorce.caseworker.event.PaymentMade.PAYMENT_MADE_FROM_AWAITING_HWF;

@ExtendWith(MockitoExtension.class)
class FromAwaitingHwfDecisionEventTest {

    @InjectMocks
    private AwaitingApplicant awaitingApplicant;

    @InjectMocks
    private HwfApplicationAccepted hwfApplicationAccepted;

    @InjectMocks
    private PaymentMade paymentMade;

    @Test
    void shouldAddConfigurationToConfigBuilderForAwaitingApplicant() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        awaitingApplicant.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(AWAITING_DOCS_FROM_AWAITING_HWF));
    }

    @Test
    void shouldAddConfigurationToConfigBuilderForHwfApplicationAccepted() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        hwfApplicationAccepted.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(HWF_APPLICATION_ACCEPTED_FROM_AWAITING_HWF_DECISION));
    }

    @Test
    void shouldAddConfigurationToConfigBuilderForPaymentMade() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        paymentMade.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(PAYMENT_MADE_FROM_AWAITING_HWF));
    }
}
