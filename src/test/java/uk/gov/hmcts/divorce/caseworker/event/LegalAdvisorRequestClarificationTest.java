package uk.gov.hmcts.divorce.caseworker.event;

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
import static uk.gov.hmcts.divorce.caseworker.event.LegalAdvisorRequestClarification.LEGAL_ADVISOR_REQUEST_CLARIFICATION;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorRequestClarificationTest {
    @InjectMocks
    private LegalAdvisorRequestClarification legalAdvisorRequestClarification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = EnumSet.allOf(State.class);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        legalAdvisorRequestClarification.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(LEGAL_ADVISOR_REQUEST_CLARIFICATION);
    }

}
