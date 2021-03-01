package uk.gov.hmcts.reform.divorce.ccd;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.divorce.ccd.framework.StubConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class DevelopmentCcdConfigTest {

    private final StubConfigBuilder<CaseData, State, UserRole> stubConfigBuilder = new StubConfigBuilder<>();
    private final DevelopmentCcdConfig developmentCcdConfig = new DevelopmentCcdConfig();

    @Test
    public void shouldAddTheDevelopmentCcdDefinitionToConfigBuilder() {

        developmentCcdConfig.configure(stubConfigBuilder);

        assertThat(stubConfigBuilder.getCaseType(), is("NO_FAULT_DIVORCE"));
        assertThat(stubConfigBuilder.getEnv(), is("development"));
    }
}