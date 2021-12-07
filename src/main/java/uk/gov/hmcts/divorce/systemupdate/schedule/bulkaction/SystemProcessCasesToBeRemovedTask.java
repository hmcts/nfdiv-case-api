package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.BulkCaseProcessingService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;

@Component
@Slf4j
public class SystemProcessCasesToBeRemovedTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Override
    public void run() {
        log.info("Processing cases to be removed from bulk case task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {
            final List<CaseDetails<BulkActionCaseData, BulkActionState>> bulkCasesWithCasesToBeRemoved =
                ccdSearchService.searchForCreatedOrListedBulkCasesWithCasesToBeRemoved(user, serviceAuth);

            log.info("Number of cases fetched which have cases to be removed: {}", bulkCasesWithCasesToBeRemoved.size());

            bulkCasesWithCasesToBeRemoved.stream()
                .filter(bulkCase -> !bulkCase.getData().getCasesToBeRemoved().isEmpty())
                .forEach(caseDetailsBulkCase -> bulkCaseProcessingService
                    .updateCasesToBeRemoved(
                        caseDetailsBulkCase,
                        SYSTEM_REMOVE_BULK_CASE,
                        bulkCaseCaseTaskFactory.getCaseTask(caseDetailsBulkCase, SYSTEM_REMOVE_BULK_CASE),
                        user,
                        serviceAuth));

            log.info("Processing cases to be removed from bulk case task completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("Processing cases to be removed from bulk case task stopped after search error", e);
        }
    }
}
