package uk.gov.hmcts.divorce.systemupdate.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class SystemMigrateCasesTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Override
    public void run() {
        log.info("Migrate cases scheduled task started");

        try {
            ccdSearchService
                .searchForCasesWithVersionLessThan(RetiredFields.getVersion())
                .parallelStream()
                .forEach(this::migrateCase);

        } catch (final CcdSearchCaseException e) {
            log.error("Case migration schedule task stopped after search error", e);
        }
    }

    private void migrateCase(CaseDetails caseDetails) {
        try {
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE);
            log.info("Migration complete for case id: {}", caseDetails.getId());
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
        }
    }

}
