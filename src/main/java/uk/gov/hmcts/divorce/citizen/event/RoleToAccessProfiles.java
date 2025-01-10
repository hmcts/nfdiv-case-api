package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CFT_TTL_MANAGER;

@Component
public class RoleToAccessProfiles implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.caseRoleToAccessProfile(CFT_TTL_MANAGER).accessProfiles("TTL_profile").build();
    }
}

