package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerRemoveCasesFromBulkList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_REMOVE_CASES_BULK_LIST = "caseworker-remove-cases-bulk-list";

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_CASES_BULK_LIST)
            .forStates(Created, Listed)
            .name("Remove cases from bulk list")
            .description("Remove cases from bulk list")
            .showSummary()
            .showEventNotes()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("removeCasesFromBulkList")
            .pageLabel("Remove cases from bulk list")
            .readonlyNoSummary(BulkActionCaseData::getCaseReferences);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(
        CaseDetails<BulkActionCaseData, BulkActionState> details
    ) {
        BulkActionCaseData caseData = details.getData();
        caseData.setCaseReferences(caseData.transformToBulkCaseElement());

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }
}
