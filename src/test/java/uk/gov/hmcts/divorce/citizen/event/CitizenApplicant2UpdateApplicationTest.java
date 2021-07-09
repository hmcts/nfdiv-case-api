package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2UpdateApplication.CITIZEN_APPLICANT2_UPDATE;

@ExtendWith(MockitoExtension.class)
public class CitizenApplicant2UpdateApplicationTest {

    @InjectMocks
    private CitizenApplicant2UpdateApplication citizenApplicant2UpdateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        citizenApplicant2UpdateApplication.configure(configBuilder);
        assertThat(configBuilder.getEvents().get(0).getId(), is(CITIZEN_APPLICANT2_UPDATE));
    }
}
