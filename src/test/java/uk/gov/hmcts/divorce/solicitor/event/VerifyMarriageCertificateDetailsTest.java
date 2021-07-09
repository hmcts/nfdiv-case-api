package uk.gov.hmcts.divorce.solicitor.event;

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
import static uk.gov.hmcts.divorce.solicitor.event.VerifyMarriageCertificate.VERIFY_MARRIAGE_CERTIFICATE_DETAILS;

@ExtendWith(MockitoExtension.class)
public class VerifyMarriageCertificateDetailsTest {

    @InjectMocks
    private VerifyMarriageCertificate verifyMarriageCertificate;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        verifyMarriageCertificate.configure(configBuilder);

        assertThat(configBuilder.getEvents().get(0).getId(), is(VERIFY_MARRIAGE_CERTIFICATE_DETAILS));
    }
}
