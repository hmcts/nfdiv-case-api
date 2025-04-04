package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService;
import uk.gov.hmcts.divorce.bulkaction.task.ProcessFailedScheduledCasesTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;

@Component
@Slf4j
public class SystemProcessFailedScheduledCasesTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Autowired
    private ProcessFailedScheduledCasesTask processFailedScheduledCasesTask;

    @Override
    public void run() {
        log.info("Processing failed scheduled cases task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final List<CaseDetails<BulkActionCaseData, BulkActionState>> listedCasesWithErrorsOrUnprocessedCases =
                ccdSearchService.searchForUnprocessedOrErroredBulkCases(Listed, user, serviceAuth);

            log.info("No of cases fetched which has unprocessed or error cases {} .", listedCasesWithErrorsOrUnprocessedCases.size());

            listedCasesWithErrorsOrUnprocessedCases
                .forEach(caseDetailsBulkCase -> bulkCaseProcessingService
                    .updateBulkCase(
                        caseDetailsBulkCase,
                        processFailedScheduledCasesTask,
                        user,
                        serviceAuth));

            log.info("Processing failed scheduled cases task completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("Processing failed scheduled cases task stopped after search error", e);
        }
    }
}
