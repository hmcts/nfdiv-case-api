package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;

@Service
@Slf4j
public class BulkCaseProcessingService {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public void updateAllBulkCases(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                   final String eventId,
                                   final CaseTask caseTask,
                                   final User user,
                                   final String serviceAuth) {

        updateBulkCase(
            bulkCaseDetails,
            bulkCaseDetails.getData().getBulkListCaseDetails(),
            eventId,
            caseTask,
            user,
            serviceAuth);
    }

    public void updateUnprocessedBulkCases(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                           final String eventId,
                                           final CaseTask caseTask,
                                           final User user,
                                           final String serviceAuth) {

        updateBulkCase(
            bulkCaseDetails,
            getFailedBulkCases(bulkCaseDetails),
            eventId,
            caseTask,
            user,
            serviceAuth);
    }

    private void updateBulkCase(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                final List<ListValue<BulkListCaseDetails>> casesToProcess,
                                final String eventId,
                                final CaseTask caseTask,
                                final User user,
                                final String serviceAuth) {

        final var bulkCaseId = bulkCaseDetails.getId();
        final var bulkActionCaseData = bulkCaseDetails.getData();

        try {
            final List<ListValue<BulkListCaseDetails>> unprocessedCases = bulkTriggerService.bulkTrigger(
                casesToProcess,
                eventId,
                caseTask,
                user,
                serviceAuth
            );

            log.info("Error bulk case details list size {} for case id {} ", unprocessedCases.size(), bulkCaseId);

            final List<ListValue<BulkListCaseDetails>> processedCases = bulkActionCaseData.calculateProcessedCases(unprocessedCases);

            log.info("Successfully processed bulk case details list size {} for case id {}", processedCases.size(), bulkCaseId);

            bulkActionCaseData.setErroredCaseDetails(unprocessedCases);
            bulkActionCaseData.setProcessedCaseDetails(processedCases);

            ccdUpdateService.submitBulkActionEvent(
                bulkCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {}, event id {} ", bulkCaseId, eventId, e);
        }
    }

    private List<ListValue<BulkListCaseDetails>> getFailedBulkCases(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {

        final var bulkCaseId = bulkCaseDetails.getId();
        final var bulkActionCaseData = bulkCaseDetails.getData();

        if (isEmpty(bulkActionCaseData.getProcessedCaseDetails())) {
            log.info("Processed cases list is empty hence processing all cases in bulk case with id {} ", bulkCaseId);
            return bulkActionCaseData.getBulkListCaseDetails();
        }

        log.info("Processed cases with errors in bulk case with id {} ", bulkCaseId);
        return bulkActionCaseData.getErroredCaseDetails();
    }
}
