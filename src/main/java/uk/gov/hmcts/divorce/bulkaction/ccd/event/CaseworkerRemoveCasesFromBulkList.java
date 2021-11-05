package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.CaseRemovalService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCasesAcceptedToListForHearing;

@Component
public class CaseworkerRemoveCasesFromBulkList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_REMOVE_CASES_BULK_LIST = "caseworker-remove-cases-bulk-list";

    @Autowired
    private CaseRemovalService caseRemovalService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_CASES_BULK_LIST)
            .forStates(Created, Listed)
            .name("Remove cases from bulk list")
            .description("Remove cases from bulk list")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SYSTEMUPDATE))
            .page("removeCasesFromBulkList", this::midEvent)
            .pageLabel("Remove cases from bulk list")
            .mandatoryNoSummary(BulkActionCaseData::getCasesAcceptedToListForHearing);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(
        CaseDetails<BulkActionCaseData, BulkActionState> details
    ) {
        BulkActionCaseData caseData = details.getData();
        caseData.setCasesAcceptedToListForHearing(caseData.transformToCasesAcceptedToListForHearing());

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> detailsBefore
    ) {
        BulkActionCaseData caseData = details.getData();
        List<String> validationErrors = validateCasesAcceptedToListForHearing(caseData);

        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
                .errors(validationErrors)
                .data(caseData)
                .build();
        }

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmit(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {
        BulkActionCaseData caseData = details.getData();

        List<String> casesAcceptedToListForHearing =
            caseData.fromListValueToList(caseData.getCasesAcceptedToListForHearing())
                .stream()
                .map(CaseLink::getCaseReference)
                .collect(toList());

        List<String> bulkListCaseDetailsToCaseReferences = caseData.getBulkListCaseDetails().stream()
            .map(c -> c.getValue().getCaseReference().getCaseReference())
            .collect(toList());

        List<String> casesToRemove = bulkListCaseDetailsToCaseReferences.stream()
            .filter(caseLink -> !casesAcceptedToListForHearing.contains(caseLink))
            .collect(toList());

        List<String> unprocessedBulkCaseIds = caseRemovalService.removeCases(details, casesToRemove, request.getHeader(AUTHORIZATION));

        if (unprocessedBulkCaseIds.isEmpty()) {
            return AboutToStartOrSubmitResponse
                .<BulkActionCaseData, BulkActionState>builder()
                .data(details.getData())
                .build();
        }

        List<String> warnings = unprocessedBulkCaseIds.stream()
            .map(unprocessedBulkCase -> String.format("Case could not be removed from Bulk case: %s", unprocessedBulkCase))
            .collect(toList());

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .warnings(warnings)
            .data(details.getData())
            .build();
    }
}
