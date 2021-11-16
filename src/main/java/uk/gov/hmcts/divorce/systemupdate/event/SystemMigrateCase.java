package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemMigrateCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_MIGRATE_CASE = "system-migrate-case";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_CASE)
            .forAllStates()
            .name("Migrate case data")
            .description("Migrate case data to the latest version")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE);
    }

}
