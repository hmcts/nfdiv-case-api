package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

@Service
@Slf4j
public class CaseTriggerService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public TriggerResult caseTrigger(final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue,
                                     final String eventId,
                                     final CaseTask caseTask,
                                     final User user,
                                     final String serviceAuth) {

        final String caseId = bulkListCaseDetailsListValue.getValue().getCaseReference().getCaseReference();

        try {
            ccdUpdateService.submitEventWithRetry(caseId, eventId, caseTask, user, serviceAuth);
            log.info("Bulk submit event completed for Case ID: {}, Event ID: {}", caseId, eventId);
            return new TriggerResult(true, bulkListCaseDetailsListValue);
        } catch (final Exception exception) {
            log.info("Bulk submit event failed for Case ID: {}, Event ID: {}", caseId, eventId);
            return new TriggerResult(false, bulkListCaseDetailsListValue);
        }
    }

    public static class TriggerResult {

        private final boolean processed;
        private final ListValue<BulkListCaseDetails> listValueBulkListCaseDetails;

        public TriggerResult(final boolean processed, final ListValue<BulkListCaseDetails> listValueBulkListCaseDetails) {
            this.processed = processed;
            this.listValueBulkListCaseDetails = listValueBulkListCaseDetails;
        }

        public boolean isProcessed() {
            return processed;
        }

        public ListValue<BulkListCaseDetails> getListValueBulkListCaseDetails() {
            return listValueBulkListCaseDetails;
        }
    }
}
