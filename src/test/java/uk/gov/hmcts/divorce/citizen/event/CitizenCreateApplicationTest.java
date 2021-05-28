package uk.gov.hmcts.divorce.citizen.event;

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
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateApplication.CITIZEN_CREATE;

@ExtendWith(MockitoExtension.class)
class CitizenCreateApplicationTest {

    @InjectMocks
    private CitizenCreateApplication citizenCreateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenCreateApplication.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(CITIZEN_CREATE));
    }
}
