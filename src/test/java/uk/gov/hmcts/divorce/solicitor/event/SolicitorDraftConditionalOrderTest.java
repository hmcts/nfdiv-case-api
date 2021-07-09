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
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorDraftConditionalOrder.SOLICITOR_DRAFT_CONDITIONAL_ORDER;

@ExtendWith(MockitoExtension.class)
class SolicitorDraftConditionalOrderTest {
    @InjectMocks
    private SolicitorDraftConditionalOrder solicitorDraftConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = EnumSet.allOf(State.class);
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        solicitorDraftConditionalOrder.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId()).isEqualTo(SOLICITOR_DRAFT_CONDITIONAL_ORDER);
    }

}
