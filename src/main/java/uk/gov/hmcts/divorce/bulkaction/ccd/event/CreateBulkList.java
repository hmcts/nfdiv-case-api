package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CreateBulkList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {
    public static final String CREATE_BULK_LIST = "create-bulk-list";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new PageBuilder<>(configBuilder
            .event(CREATE_BULK_LIST)
            .initialState(Created)
            .name("Create bulk list")
            .description("Create bulk list")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE));
    }
}
