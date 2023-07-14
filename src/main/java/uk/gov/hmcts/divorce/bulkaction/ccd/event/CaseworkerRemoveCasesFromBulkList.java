package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionPageBuilder;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseRemovalService;
import uk.gov.hmcts.divorce.bulkaction.service.PronouncementListDocService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Created;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCasesAcceptedToListForHearing;

@Component
@Slf4j
public class CaseworkerRemoveCasesFromBulkList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String CASEWORKER_REMOVE_CASES_BULK_LIST = "caseworker-remove-cases-bulk-list";

    @Autowired
    private CaseRemovalService caseRemovalService;

    @Autowired
    private PronouncementListDocService pronouncementListDocService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_CASES_BULK_LIST)
            .forStates(Created, Listed, Pronounced)
            .name("Remove cases from bulk list")
            .description("Remove cases from bulk list")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
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

        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_REMOVE_CASES_BULK_LIST, details.getId());

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

        log.info("{} mid event callback invoked for Case Id: {}", CASEWORKER_REMOVE_CASES_BULK_LIST, details.getId());

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
        final CaseDetails<BulkActionCaseData, BulkActionState> details,
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_REMOVE_CASES_BULK_LIST, details.getId());

        BulkActionCaseData bulkActionCaseData = details.getData();

        List<String> casesAcceptedToListForHearing =
            bulkActionCaseData.fromListValueToList(bulkActionCaseData.getCasesAcceptedToListForHearing())
                .stream()
                .map(CaseLink::getCaseReference).toList();

        List<ListValue<BulkListCaseDetails>> casesToRemove =
            bulkActionCaseData.getBulkListCaseDetails().stream()
                .filter(c -> !casesAcceptedToListForHearing.contains(c.getValue().getCaseReference().getCaseReference()))
                .collect(toList());

        bulkActionCaseData.setBulkListCaseDetails(
            bulkActionCaseData.getBulkListCaseDetails().stream()
                .filter(lv -> !casesToRemove.contains(lv))
                .collect(toList()));
        bulkActionCaseData.setCasesToBeRemoved(casesToRemove);

        if (bulkActionCaseData.getPronouncementListDocument() != null) {
            pronouncementListDocService.generateDocument(details);
        }

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails
    ) {

        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_REMOVE_CASES_BULK_LIST, details.getId());

        caseRemovalService.removeCases(details);

        return SubmittedCallbackResponse.builder().build();
    }
}
