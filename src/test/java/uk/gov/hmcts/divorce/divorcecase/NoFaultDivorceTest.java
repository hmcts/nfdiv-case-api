package uk.gov.hmcts.divorce.divorcecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
public class NoFaultDivorceTest {

    @InjectMocks
    private NoFaultDivorce noFaultDivorce;

    @Test
    void shouldSetTheCorrectCaseTypeName() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        noFaultDivorce.configure(configBuilder);

        assertThat(configBuilder.build().getCaseDesc()).isEqualTo("Handling of the dissolution of marriage");

    }

}
