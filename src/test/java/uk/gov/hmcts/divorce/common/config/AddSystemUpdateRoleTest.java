package uk.gov.hmcts.divorce.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;

@ExtendWith(MockitoExtension.class)
public class AddSystemUpdateRoleTest {

    @InjectMocks
    private AddSystemUpdateRole addSystemUpdateRole;

    @Test
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAat() throws Exception {
        List<UserRole> actualRoles =
            withEnvironmentVariable("ENVIRONMENT", "aat")
                .execute(() -> addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN))
                );

        assertThat(actualRoles).containsExactlyInAnyOrder(CITIZEN, CASEWORKER_SYSTEMUPDATE);
    }
}
