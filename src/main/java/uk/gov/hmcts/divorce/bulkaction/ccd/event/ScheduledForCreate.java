package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class ScheduledForCreate implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String SCHEDULE_CREATE = "scheduleCreate";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(SCHEDULE_CREATE)
            .forStates(BulkActionState.ScheduledForCreate)
            .name("Schedule bulk list create")
            .description("Schedule bulk list created")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE,
                CASEWORKER_SUPERUSER,
                CASEWORKER_COURTADMIN_CTSC))
            .page("scheduleCreate")
            .pageLabel("Schedule bulk list")
            .optional(BulkActionCaseData::getCaseTitle);// Temporarily setting field needs to be changed later
    }
}
