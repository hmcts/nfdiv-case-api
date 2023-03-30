package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;

@Service
@Slf4j
public class BulkCaseProcessingService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public void updateAllBulkCases(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                   final BulkCaseTask bulkCaseTask,
                                   final User user,
                                   final String serviceAuth) {

        updateBulkCase(
            bulkCaseDetails,
            bulkCaseTask,
            user,
            serviceAuth);
    }

    private void updateBulkCase(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                final BulkCaseTask bulkCaseTask,
                                final User user,
                                final String serviceAuth) {
        try {
            ccdUpdateService.submitBulkActionEvent(
                bulkCaseTask,
                bulkCaseDetails.getId(),
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {}, event id {} ", bulkCaseDetails.getId(), SYSTEM_UPDATE_BULK_CASE, e);
        }
    }
}
