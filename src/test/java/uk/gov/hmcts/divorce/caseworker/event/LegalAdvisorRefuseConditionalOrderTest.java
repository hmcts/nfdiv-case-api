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
import static uk.gov.hmcts.divorce.caseworker.event.LegalAdvisorGrantConditionalOrder.LEGAL_ADVISOR_GRANT_CONDITIONAL_ORDER;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorRefuseConditionalOrderTest {
    @InjectMocks
    private LegalAdvisorGrantConditionalOrder legalAdvisorGrantConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = EnumSet.allOf(State.class);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        legalAdvisorGrantConditionalOrder.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(LEGAL_ADVISOR_GRANT_CONDITIONAL_ORDER);
    }

}
