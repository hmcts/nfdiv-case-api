package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

@Service
@Slf4j
public class CaseTriggerService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public TriggerResult caseTrigger(final BulkListCaseDetails bulkListCaseDetails,
                                     final String eventId,
                                     final CaseTask caseTask,
                                     final User user,
                                     final String serviceAuth) {

        final String caseId = bulkListCaseDetails.getCaseReference().getCaseReference();

        try {
            ccdUpdateService.submitEventWithRetry(caseId, eventId, caseTask, user, serviceAuth);
            log.info("Bulk submit event completed for Case ID: {}, Event ID: {}", caseId, eventId);
            return new TriggerResult(true, bulkListCaseDetails);
        } catch (final Exception exception) {
            log.info("Bulk submit event failed for Case ID: {}, Event ID: {}", caseId, eventId);
            return new TriggerResult(false, bulkListCaseDetails);
        }
    }

    public static class TriggerResult {

        private final boolean processed;
        private final BulkListCaseDetails bulkListCaseDetails;

        public TriggerResult(final boolean processed, final BulkListCaseDetails bulkListCaseDetails) {
            this.processed = processed;
            this.bulkListCaseDetails = bulkListCaseDetails;
        }

        public boolean isProcessed() {
            return processed;
        }

        public BulkListCaseDetails getBulkListCaseDetails() {
            return bulkListCaseDetails;
        }
    }
}
