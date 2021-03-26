package uk.gov.hmcts.reform.divorce.ccd.caserole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseRole;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
class CaseRolesTest {

    @Mock
    private ConfigBuilder configBuilder;

    @Captor
    private ArgumentCaptor<Set<CaseRole.CaseRoleBuilder>> caseRoleBuilders;

    @InjectMocks
    private CaseRoles caseRoles;

    @SuppressWarnings("unchecked")
    @Test
    void shouldApplyCaseRolesToConfigBuilder() {

        final CaseRole expectedCaseRole1 = CaseRole.builder()
            .id("[RESPSOLICITOR]")
            .name("Respondent's Solicitor")
            .description("Role to isolate events available for Respondent's Solicitor")
            .build();
        final CaseRole expectedCaseRole2 = CaseRole.builder()
            .id("[PETSOLICITOR]")
            .name("Petitioner's Solicitor")
            .description("Role to isolate events available for Petitioner's Solicitor")
            .build();

        doNothing().when(configBuilder).add(caseRoleBuilders.capture());

        caseRoles.applyTo(configBuilder);

        assertThat(getCapturedCaseRoles(), containsInAnyOrder(expectedCaseRole1, expectedCaseRole2));
        verify(configBuilder).add(anySet());
        verifyNoMoreInteractions(configBuilder);
    }

    private List<CaseRole> getCapturedCaseRoles() {
        return caseRoleBuilders.getValue().stream()
            .map(CaseRole.CaseRoleBuilder::build)
            .collect(toList());
    }
}