package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.ErroredBulkCasesService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

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
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private ErroredBulkCasesService erroredBulkCasesService;

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
                .forEach(caseDetailsBulkCase -> erroredBulkCasesService
                    .processErroredCasesAndUpdateBulkCase(
                        caseDetailsBulkCase,
                        SYSTEM_UPDATE_CASE_COURT_HEARING,
                        bulkCaseCaseTaskFactory.getCaseTask(caseDetailsBulkCase.getData(), SYSTEM_UPDATE_CASE_COURT_HEARING),
                        user,
                        serviceAuth));

            log.info("Processing failed scheduled cases task completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("Processing failed scheduled cases task stopped after search error", e);
        }
    }
}
