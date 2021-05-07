package uk.gov.hmcts.divorce.solicitor.event;

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
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorUpdateLanguage.SOLICITOR_UPDATE_LANGUAGE;

@ExtendWith(MockitoExtension.class)
public class SolicitorUpdateLanguageTest {

    @InjectMocks
    private SolicitorUpdateLanguage solicitorUpdateLanguage;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        solicitorUpdateLanguage.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(SOLICITOR_UPDATE_LANGUAGE));
    }
}
