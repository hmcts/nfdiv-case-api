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
import uk.gov.hmcts.divorce.bulkaction.service.PronouncementListDocService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Empty;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCasesAcceptedToListForHearing;

@Component
@Slf4j
public class SuperuserRemoveCaseFromBulkList implements CCDConfig<BulkActionCaseData, BulkActionState, UserRole> {

    public static final String SUPERUSER_REMOVE_CASE_BULK_LIST = "superuser-remove-cases-bulk-list";

    @Autowired
    private PronouncementListDocService pronouncementListDocService;

    @Override
    public void configure(final ConfigBuilder<BulkActionCaseData, BulkActionState, UserRole> configBuilder) {
        new BulkActionPageBuilder(configBuilder
            .event(SUPERUSER_REMOVE_CASE_BULK_LIST)
            .forStates(Empty)
            .name("Super remove case from list")
            .description("Superuser remove case from bulk list")
            .showSummary()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .explicitGrants()
            //.grant(CREATE_READ_UPDATE, SUPER_USER)
            )
            .page("removeCaseFromBulkList", this::midEvent)
            .pageLabel("Remove case from bulk list")
            .mandatoryNoSummary(BulkActionCaseData::getCasesAcceptedToListForHearing);
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToStart(
        CaseDetails<BulkActionCaseData, BulkActionState> details) {

        log.info("{} about to start callback invoked for Case Id: {}", SUPERUSER_REMOVE_CASE_BULK_LIST, details.getId());

        BulkActionCaseData caseData = details.getData();
        caseData.setCasesAcceptedToListForHearing(caseData.transformToCasesAcceptedToListForHearing());

        return AboutToStartOrSubmitResponse.<BulkActionCaseData, BulkActionState>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> midEvent(
        CaseDetails<BulkActionCaseData, BulkActionState> details,
        CaseDetails<BulkActionCaseData, BulkActionState> detailsBefore) {

        log.info("{} mid event callback invoked for Case Id: {}", SUPERUSER_REMOVE_CASE_BULK_LIST, details.getId());

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
        final CaseDetails<BulkActionCaseData, BulkActionState> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SUPERUSER_REMOVE_CASE_BULK_LIST, details.getId());

        final BulkActionCaseData bulkActionCaseData = details.getData();

        final List<String> casesAcceptedToListForHearing =
            bulkActionCaseData.fromListValueToList(bulkActionCaseData.getCasesAcceptedToListForHearing())
                .stream()
                .map(CaseLink::getCaseReference)
                .toList();

        final List<ListValue<BulkListCaseDetails>> casesToRemove =
            bulkActionCaseData.getBulkListCaseDetails().stream()
                .filter(c -> !casesAcceptedToListForHearing.contains(c.getValue().getCaseReference().getCaseReference()))
                .toList();

        bulkActionCaseData.setBulkListCaseDetails(
            bulkActionCaseData.getBulkListCaseDetails().stream()
                .filter(lv -> !casesToRemove.contains(lv))
                .toList());

        if (nonNull(bulkActionCaseData.getErroredCaseDetails())) {
            bulkActionCaseData.setErroredCaseDetails(
                bulkActionCaseData.getErroredCaseDetails().stream()
                    .filter(c -> casesAcceptedToListForHearing.contains(c.getValue().getCaseReference().getCaseReference()))
                    .toList());
        }

        if (nonNull(bulkActionCaseData.getPronouncementListDocument())) {
            pronouncementListDocService.generateDocument(details);
        }

        return AboutToStartOrSubmitResponse
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();
    }
}
