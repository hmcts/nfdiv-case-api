package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitAos.SOLICITOR_SUBMIT_AOS;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitAosTest {
    @InjectMocks
    private SolicitorSubmitAos solicitorSubmitAos;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = EnumSet.allOf(State.class);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        solicitorSubmitAos.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(SOLICITOR_SUBMIT_AOS);
    }

}
