package uk.gov.hmcts.divorce.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;

@ExtendWith(MockitoExtension.class)
public class AddSystemUpdateRoleTest {

    @InjectMocks
    private AddSystemUpdateRole addSystemUpdateRole;

    @Test
    @SetEnvironmentVariable(
        key = "ENVIRONMENT",
        value = "aat")
    public void shouldAddSystemUpdateRoleWhenEnvironmentIsAat() {
        List<UserRole> actualRoles = addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(CITIZEN));

        assertThat(actualRoles).containsExactlyInAnyOrder(CITIZEN, CASEWORKER_SYSTEMUPDATE);
    }

    @Test
    public void shouldNotAddSystemUpdateRoleWhenEnvironmentIsNotAat() {
        List<UserRole> actualRoles = addSystemUpdateRole.addIfConfiguredForEnvironment(List.of(SOLICITOR));

        assertThat(actualRoles).containsExactlyInAnyOrder(SOLICITOR);
    }
}
