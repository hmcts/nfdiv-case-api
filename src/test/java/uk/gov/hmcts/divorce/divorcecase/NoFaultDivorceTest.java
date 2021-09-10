package uk.gov.hmcts.divorce.divorcecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;

@ExtendWith(MockitoExtension.class)
public class NoFaultDivorceTest {

    @Mock
    private AddSystemUpdateRole addSystemUpdateRole;

    @InjectMocks
    private NoFaultDivorce noFaultDivorce;

    @Test
    void shouldAddSystemUpdateUserAccessToDraftStateWhenEnvironmentIsAat() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.isEnvironmentAat()).thenReturn(true);

        noFaultDivorce.configure(configBuilder);

        assertThat(configBuilder.build().getCaseType()).isEqualTo("NFD");

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(CITIZEN))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(SYSTEMUPDATE))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(SOLICITOR))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(SUPER_USER))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(LEGAL_ADVISOR))
            .contains(entry(Draft, Set.of(R)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(CASE_WORKER))
            .contains(entry(Draft, Set.of(R)));

        verify(addSystemUpdateRole).isEnvironmentAat();
    }

    @Test
    void shouldNotAddSystemUpdateUserAccessToDraftStateWhenEnvironmentIsNotAat() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.isEnvironmentAat()).thenReturn(false);

        noFaultDivorce.configure(configBuilder);

        assertThat(configBuilder.build().getCaseType()).isEqualTo("NFD");

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(CITIZEN))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(SOLICITOR))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(SUPER_USER))
            .contains(entry(Draft, Set.of(C, R, U)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(LEGAL_ADVISOR))
            .contains(entry(Draft, Set.of(R)));

        assertThat(configBuilder.build().getStateRolePermissions().columnMap().get(CASE_WORKER))
            .contains(entry(Draft, Set.of(R)));

        verify(addSystemUpdateRole).isEnvironmentAat();
    }
}
