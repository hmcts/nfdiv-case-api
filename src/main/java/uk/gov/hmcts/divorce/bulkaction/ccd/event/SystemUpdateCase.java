package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Dropped;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemUpdateCase implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String SYSTEM_UPDATE_BULK_CASE = "system-update-case";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(SYSTEM_UPDATE_BULK_CASE)
            .forStates(Created, Listed, Pronounced, Dropped)
            .name("System update case")
            .description("System update case")
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE)
            .grantHistoryOnly(LEGAL_ADVISOR))
            .page("EDIT")
            .pageLabel("SYSTEM EDIT")
            .mandatory(BulkActionCaseData::getBulkListCaseDetails)
            .mandatory(BulkActionCaseData::getErroredCaseDetails)
            .mandatory(BulkActionCaseData::getProcessedCaseDetails)
            .mandatory(BulkActionCaseData::getCasesToBeRemoved)
            .mandatory(BulkActionCaseData::getCasesAcceptedToListForHearing);
    }
}
