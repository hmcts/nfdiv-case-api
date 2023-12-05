package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkCaseRetiredFields;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.MigrateBulkCaseRetiredFields;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.SetFailedBulkCaseMigrationVersionToZero;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateBulkCase.SYSTEM_MIGRATE_BULK_CASE;

@Component
@Slf4j
public class SystemMigrateBulkCasesTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private MigrateBulkCaseRetiredFields migrateBulkCaseRetiredFields;

    @Autowired
    private SetFailedBulkCaseMigrationVersionToZero setFailedBulkCaseMigrationVersionToZero;

    @Override
    public void run() {
        log.info("Migrate cases scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            ccdSearchService
                .searchForBulkCasesWithVersionLessThan(BulkCaseRetiredFields.getVersion(), user, serviceAuthorization)
                .parallelStream()
                .forEach(details -> migrateCase(details, user, serviceAuthorization));

        } catch (final CcdSearchCaseException e) {
            log.error("Case migration schedule task stopped after search error", e);
        }
    }

    private void migrateCase(final CaseDetails caseDetails, final User user, final String serviceAuthorization) {
        final Long caseId = caseDetails.getId();

        try {
            ccdUpdateService.updateBulkCaseWithRetries(
                migrateBulkCaseRetiredFields,
                SYSTEM_MIGRATE_BULK_CASE,
                user,
                serviceAuthorization,
                caseId
            );
            log.info("Migration complete for case id: {}", caseId);
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);

            if (e.getStatus() != NOT_FOUND.value()) {
                log.info("Setting bulkCaseDataVersion to 0 for case id: {} after failed migration", caseId);
                ccdUpdateService.updateBulkCaseWithRetries(
                    setFailedBulkCaseMigrationVersionToZero,
                    SYSTEM_MIGRATE_BULK_CASE,
                    user,
                    serviceAuthorization,
                    caseId
                );
                log.info("bulkCaseDataVersion set for case id: {}", caseId);
            } else {
                log.info("Version not set to 0 case not found for case id: {}", caseId);
            }
        }
    }
}
