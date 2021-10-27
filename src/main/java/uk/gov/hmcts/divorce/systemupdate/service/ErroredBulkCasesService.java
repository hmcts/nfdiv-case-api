package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCaseErrors.SYSTEM_BULK_CASE_ERRORS;

@Service
@Slf4j
public class ErroredBulkCasesService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BulkTriggerService bulkTriggerService;

    public void processErroredCasesAndUpdateBulkCase(final CaseDetails caseDetailsBulkCase,
                                                     final String eventId,
                                                     final User user,
                                                     final String serviceAuth) {

        final var bulkCaseId = caseDetailsBulkCase.getId();

        try {
            final var bulkCaseData = objectMapper.convertValue(caseDetailsBulkCase.getData(), BulkActionCaseData.class);

            final List<ListValue<BulkListCaseDetails>> failedBulkCases = getFailedBulkCases(bulkCaseId, bulkCaseData);

            final List<ListValue<BulkListCaseDetails>> unprocessed = bulkTriggerService.bulkTrigger(
                failedBulkCases,
                eventId,
                caseDetails -> caseDetails,
                user,
                serviceAuth);

            final List<ListValue<BulkListCaseDetails>> processedCases = bulkCaseData.calculateProcessedCases(unprocessed);

            bulkCaseData.setErroredCaseDetails(unprocessed);
            bulkCaseData.setProcessedCaseDetails(processedCases);

            caseDetailsBulkCase.setData(objectMapper.convertValue(bulkCaseData, new TypeReference<>() {
            }));

            ccdUpdateService.updateBulkCaseWithRetries(
                caseDetailsBulkCase,
                SYSTEM_BULK_CASE_ERRORS,
                user,
                serviceAuth,
                bulkCaseId);
        } catch (final CcdManagementException e) {
            log.error(
                "Retry bulk case pronounced errors schedule task, bulk case update failed with exception for BulkCase Id: {}",
                bulkCaseId,
                e);
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
