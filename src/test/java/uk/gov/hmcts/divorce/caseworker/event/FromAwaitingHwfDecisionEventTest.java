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
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAwaitingApplicantFromAwaitingHwf.CASEWORKER_AWAITING_DOC_FROM_AWAITING_HWF;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerHwfApplicationAccepted.CASEWORKER_HWF_APPLICATION_ACCEPTED;

@ExtendWith(MockitoExtension.class)
class FromAwaitingHwfDecisionEventTest {

    @InjectMocks
    private CaseworkerAwaitingApplicantFromAwaitingHwf caseworkerAwaitingApplicantFromAwaitingHwf;

    @InjectMocks
    private CaseworkerHwfApplicationAccepted caseworkerHwfApplicationAccepted;

    @Test
    void shouldAddConfigurationToConfigBuilderForAwaitingApplicant() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerAwaitingApplicantFromAwaitingHwf.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_AWAITING_DOC_FROM_AWAITING_HWF);
    }

    @Test
    void shouldAddConfigurationToConfigBuilderForHwfApplicationAccepted() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerHwfApplicationAccepted.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_HWF_APPLICATION_ACCEPTED);
    }
}
