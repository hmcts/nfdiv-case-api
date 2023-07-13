package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemMigrateBulkCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String SYSTEM_MIGRATE_BULK_CASE = "system-migrate-bulk-case";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_MIGRATE_BULK_CASE)
            .forAllStates()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name("Migrate bulk case data")
            .description("Migrate bulk case data to the latest version")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails) {

        log.info("Migrating bulk case data for case Id: {}", bulkCaseDetails.getId());

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkCaseDetails.getData())
            .build();
    }
}
