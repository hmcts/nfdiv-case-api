package uk.gov.hmcts.divorce.citizen.event;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.divorce.common.AddSystemUpdateRole;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.api.Permission.C;
import static uk.gov.hmcts.ccd.sdk.api.Permission.R;
import static uk.gov.hmcts.ccd.sdk.api.Permission.U;
import static uk.gov.hmcts.divorce.citizen.event.CitizenCreateApplication.CITIZEN_CREATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CitizenCreateApplicationTest {

    @Mock
    private AddSystemUpdateRole addSystemUpdateRole;

    @InjectMocks
    private CitizenCreateApplication citizenCreateApplication;

    @Test
    void shouldAddConfigurationToConfigBuilderAndSetPermissionOnlyForCitizenRole() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(CITIZEN));

        citizenCreateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_CREATE);

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(CITIZEN, C)
            .put(CITIZEN, R)
            .put(CITIZEN, U)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactly(expectedRolesAndPermissions);
    }

    @Test
    void shouldSetPermissionForCitizenAndSystemUpdateRoleWhenEnvironmentIsAat() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        when(addSystemUpdateRole.addIfConfiguredForEnvironment(anyList()))
            .thenReturn(List.of(CITIZEN, CASEWORKER_SYSTEMUPDATE));

        citizenCreateApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_CREATE);

        SetMultimap<UserRole, Permission> expectedRolesAndPermissions = ImmutableSetMultimap.<UserRole, Permission>builder()
            .put(CITIZEN, C)
            .put(CITIZEN, R)
            .put(CITIZEN, U)
            .put(CASEWORKER_SYSTEMUPDATE, C)
            .put(CASEWORKER_SYSTEMUPDATE, R)
            .put(CASEWORKER_SYSTEMUPDATE, U)
            .build();

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getGrants)
            .containsExactlyInAnyOrder(expectedRolesAndPermissions);

        verify(addSystemUpdateRole).addIfConfiguredForEnvironment(anyList());
    }
}
