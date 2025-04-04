package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.UpdateCasesToBeRemovedTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
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
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private UpdateCasesToBeRemovedTask updateCasesToBeRemovedTask;

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
                .forEach(caseDetailsBulkCase -> updateCasesToBeRemoved(
                    caseDetailsBulkCase,
                    SYSTEM_REMOVE_BULK_CASE,
                    user,
                    serviceAuth)
                );

            log.info("Processing cases to be removed from bulk case task completed.");
        } catch (final CcdSearchCaseException e) {
            log.error("Processing cases to be removed from bulk case task stopped after search error", e);
        }
    }

    public void updateCasesToBeRemoved(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                       final String eventId,
                                       final User user,
                                       final String serviceAuth) {

        final var bulkCaseId = bulkCaseDetails.getId();

        try {
            ccdUpdateService.submitBulkActionEvent(
                updateCasesToBeRemovedTask,
                bulkCaseId,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {}, event id {} ", bulkCaseId, eventId, e);
        }
    }
}
