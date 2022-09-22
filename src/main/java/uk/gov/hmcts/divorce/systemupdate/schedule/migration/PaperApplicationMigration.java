package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
public class PaperApplicationMigration implements Migration {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Override
    public void apply(final User user, final String serviceAuthorization) {
        try {
            log.info("Started migrating cases joint paper application");
            ccdSearchService
                .searchJointPaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, serviceAuthorization)
                .parallelStream()
                .forEach(details -> setApplicant2Offline(details, user, serviceAuthorization));

        } catch (final CcdSearchCaseException e) {
            log.error("Case schedule task (migration joint paper application) stopped after search error", e);
        }

        try {
            log.info("Started migrating cases sole paper application");
            ccdSearchService
                .searchSolePaperApplicationsWhereApplicant2OfflineFlagShouldBeSet(user, serviceAuthorization)
                .parallelStream()
                .forEach(details -> setApplicant2Offline(details, user, serviceAuthorization));

        } catch (final CcdSearchCaseException e) {
            log.error("Case schedule task (migration sole paper application) stopped after search error", e);
        }
    }

    private void setApplicant2Offline(final CaseDetails caseDetails, final User user, final String serviceAuthorization) {
        final Long caseId = caseDetails.getId();

        try {
            caseDetails.getData().put("applicant2Offline", YesOrNo.YES);
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);
            log.info("Set applicant2Offline field to Yes successfully for case id: {}", caseId);
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            log.error("Submit event (set setApplicant2Offline field) failed for case id: {}, continuing to next case", caseId);
        }
    }
}
