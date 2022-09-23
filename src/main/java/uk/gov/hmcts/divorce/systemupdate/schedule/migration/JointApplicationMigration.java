package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class JointApplicationMigration implements Migration {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Override
    public void apply(final User user, final String serviceAuthorization) {
        log.info("Started migrating cases joint application");
        try {
            ccdSearchService
                .searchJointApplicationsWithAccessCodePostIssueApplication(user, serviceAuthorization)
                .parallelStream()
                .forEach(details -> removeAccessCode(details, user, serviceAuthorization));

        } catch (final CcdSearchCaseException e) {
            log.error("Case schedule task(migration joint application) stopped after search error", e);
        }
    }

    private void removeAccessCode(final CaseDetails caseDetails, final User user, final String serviceAuthorization) {
        final Long caseId = caseDetails.getId();

        try {
            caseDetails.getData().put("accessCode", "");
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);
            log.info("Removed access code successfully for case id: {}", caseId);
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            log.error("Submit event(after removing access code) failed for case id: {}, continuing to next case", caseId);
        }
    }
}
