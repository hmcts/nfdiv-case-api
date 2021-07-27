package uk.gov.hmcts.divorce.citizen.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;

import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CitizenCreateApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_CREATE = "citizen-create-application";
    private static final String ENVIRONMENT_AAT = "aat";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var roles = new ArrayList<UserRole>();
        roles.add(CITIZEN);

        String environment = System.getenv().getOrDefault("ENVIRONMENT", null);

        if (null != environment && environment.equalsIgnoreCase(ENVIRONMENT_AAT)) {
            roles.add(CASEWORKER_SYSTEMUPDATE);
        }
        configBuilder
            .event(CITIZEN_CREATE)
            .initialState(Draft)
            .name("Create draft case")
            .description("Apply for a divorce or dissolution")
            .grant(CREATE_READ_UPDATE, roles.toArray(UserRole[]::new))
            .retries(120, 120);
    }
}
