package uk.gov.hmcts.divorce.divorcecase.model.access;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Arrays;

@Component
public class RoleToAccessProfiles implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        Arrays.stream(UserRole.values()).forEach(
                userRole -> {
                    if (userRole.getRole().contains("caseworker")) {
                        configBuilder.caseRoleToAccessProfile(userRole).legacyIdamRole()
                                .accessProfiles(userRole.getRole(), "TTL_profile").build();
                    }
                }
        );
    }
}
