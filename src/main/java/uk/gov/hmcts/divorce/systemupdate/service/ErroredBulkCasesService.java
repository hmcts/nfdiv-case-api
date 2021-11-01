package uk.gov.hmcts.divorce.systemupdate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;

@Service
@Slf4j
public class ErroredBulkCasesService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private BulkTriggerService bulkTriggerService;

    public void processErroredCasesAndUpdateBulkCase(
        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetailsBulkCase,
        final String eventId,
        final CaseTask caseTask,
        final User user,
        final String serviceAuth) {

        final var bulkCaseId = caseDetailsBulkCase.getId();

        try {
            final var bulkCaseData = caseDetailsBulkCase.getData();

            final List<ListValue<BulkListCaseDetails>> failedBulkCases = getFailedBulkCases(bulkCaseId, bulkCaseData);

            final List<ListValue<BulkListCaseDetails>> unprocessed = bulkTriggerService.bulkTrigger(
                failedBulkCases,
                eventId,
                caseTask,
                user,
                serviceAuth);

            log.info("Error bulk case details list size {} for case id {} ", unprocessed.size(), bulkCaseId);

            final List<ListValue<BulkListCaseDetails>> processedCases = bulkCaseData.calculateProcessedCases(unprocessed);

            log.info("Successfully processed bulk case details list size {} for case id {}", processedCases.size(), bulkCaseId);

            bulkCaseData.setErroredCaseDetails(unprocessed);
            bulkCaseData.setProcessedCaseDetails(processedCases);

            ccdUpdateService.submitBulkActionEvent(
                caseDetailsBulkCase,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {}, event id {} ", bulkCaseId, eventId, e);
        }
    }

    private List<ListValue<BulkListCaseDetails>> getFailedBulkCases(final Long bulkCaseId,
                                                                    final BulkActionCaseData bulkCaseData) {
        if (isEmpty(bulkCaseData.getProcessedCaseDetails())) {
            log.info("Processed cases list is empty hence processing all cases in bulk case with id {} ", bulkCaseId);
            return bulkCaseData.getBulkListCaseDetails();
        }

        log.info("Processed cases with errors in bulk case with id {} ", bulkCaseId);
        return bulkCaseData.getErroredCaseDetails();
    }
}
