package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Component
@Slf4j
public class SystemProcessFailedScheduledCasesTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private ScheduleCaseService scheduleCaseService;

    @Override
    public void run() {
        log.info("Processing failed scheduled cases task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final List<CaseDetails> listedCasesWithErrorsOrUnprocessedCases =
                ccdSearchService.searchForBulkCasesWithCaseErrorsAndState(Listed, user, serviceAuth);
            log.info("No of cases fetched which has unprocessed or error cases {} .", listedCasesWithErrorsOrUnprocessedCases.size());
            listedCasesWithErrorsOrUnprocessedCases.forEach(bulkCaseDetails -> processFailedCases(bulkCaseDetails, user, serviceAuth));

            log.info("Processing failed scheduled cases task completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("Processing failed scheduled cases task stopped after search error", e);
        }
    }

    private void processFailedCases(final CaseDetails bulkCaseDetails,
                                    final User user,
                                    final String serviceAuth) {
        final var bulkCaseData = objectMapper.convertValue(bulkCaseDetails.getData(), BulkActionCaseData.class);

        final Long bulkCaseId = bulkCaseDetails.getId();
        log.info("Processing bulk case with id {} ", bulkCaseId);
        List<ListValue<BulkListCaseDetails>> bulkCasesTobeReprocessed;

        if (CollectionUtils.isEmpty(bulkCaseData.getProcessedCaseDetails())) {
            log.info("Processed cases list is empty hence processing all cases in bulk case with id {} ", bulkCaseId);
            bulkCasesTobeReprocessed = bulkCaseData.getBulkListCaseDetails();
        } else {
            log.info("Processed cases with errors in bulk case with id {} ", bulkCaseId);
            bulkCasesTobeReprocessed = bulkCaseData.getErroredCaseDetails();
        }

        final List<ListValue<BulkListCaseDetails>> unprocessedBulkCases = bulkTriggerService.bulkTrigger(
            bulkCasesTobeReprocessed,
            SYSTEM_UPDATE_CASE_COURT_HEARING,
            scheduleCaseService.getCaseTask(bulkCaseData),
            user,
            serviceAuth);

        log.info("Error bulk case details list size {} for case id {} ", unprocessedBulkCases.size(), bulkCaseId);

        List<ListValue<BulkListCaseDetails>> processedBulkCases = bulkCaseData.calculateProcessedCases(unprocessedBulkCases);

        log.info("Successfully processed bulk case details list size {} for case id {}", processedBulkCases.size(), bulkCaseId);

        bulkCaseData.setErroredCaseDetails(unprocessedBulkCases);
        bulkCaseData.setProcessedCaseDetails(processedBulkCases);

        bulkCaseDetails.setData(objectMapper.convertValue(bulkCaseData, new TypeReference<>() {
        }));

        try {
            ccdUpdateService.updateBulkCaseWithRetries(
                bulkCaseDetails,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth,
                bulkCaseId);
        } catch (final FeignException e) {
            log.error("Update failed for bulk case id {} ", bulkCaseId, e);
        }
    }
}
