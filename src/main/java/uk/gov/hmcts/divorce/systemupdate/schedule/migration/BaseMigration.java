package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.MigrateRetiredFields;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.SetFailedMigrationVersionToZero;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class BaseMigration implements Migration {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private MigrateRetiredFields migrateRetiredFields;

    @Autowired
    private SetFailedMigrationVersionToZero setFailedMigrationVersionToZero;

    @Override
    public void apply(final User user, final String serviceAuthorization) {
        try {
            ccdSearchService
                .searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, serviceAuthorization)
                .parallelStream()
                .forEach(details -> migrateCase(details, user, serviceAuthorization));

        } catch (final CcdSearchCaseException e) {
            log.error("Case migration schedule task stopped after search error", e);
        }
    }

    private void migrateCase(final CaseDetails caseDetails, final User user, final String serviceAuthorization) {
        final String caseId = caseDetails.getId().toString();

        try {
            ccdUpdateService.submitEventWithRetry(caseId, SYSTEM_MIGRATE_CASE, migrateRetiredFields, user, serviceAuthorization);
            log.info("Migration complete for case id: {}", caseId);
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            if (e.getStatus() != NOT_FOUND.value()) {
                log.error("Submit event failed for case id: {}, setting dataVersion to highest priority. Caused by: {}",
                    caseId, e.getMessage());
                ccdUpdateService.submitEventWithRetry(
                    caseId,
                    SYSTEM_MIGRATE_CASE,
                    setFailedMigrationVersionToZero,
                    user,
                    serviceAuthorization);
            }
        } catch (final IllegalArgumentException e) {
            log.error("Submit event failed for case id: {}, setting dataVersion to highest priority. Caused by: {}",
                caseId, e.getMessage());
            ccdUpdateService.submitEventWithRetry(
                caseId,
                SYSTEM_MIGRATE_CASE,
                setFailedMigrationVersionToZero,
                user,
                serviceAuthorization);
        }
    }
}
