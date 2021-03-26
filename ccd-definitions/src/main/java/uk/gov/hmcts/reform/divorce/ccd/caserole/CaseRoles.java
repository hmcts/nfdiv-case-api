package uk.gov.hmcts.reform.divorce.ccd.caserole;

import uk.gov.hmcts.ccd.sdk.api.CaseRole;
import uk.gov.hmcts.ccd.sdk.api.CaseRole.CaseRoleBuilder;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import java.util.HashSet;
import java.util.Set;

public class CaseRoles implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.add(caseRoles());
    }

    private Set<CaseRoleBuilder> caseRoles() {

        final Set<CaseRoleBuilder> caseRoles = new HashSet<>();

        caseRoles.add(CaseRole.builder()
            .id("[RESPSOLICITOR]")
            .name("Respondent's Solicitor")
            .description("Role to isolate events available for Respondent's Solicitor"));

        caseRoles.add(CaseRole.builder()
            .id("[PETSOLICITOR]")
            .name("Petitioner's Solicitor")
            .description("Role to isolate events available for Petitioner's Solicitor"));

        return caseRoles;
    }

}
