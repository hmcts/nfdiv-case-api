package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.UpdateConfirmReadPetitionFields;
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
public class ConfirmReadPetitionMigration implements Migration {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private UpdateConfirmReadPetitionFields updateConfirmReadPetitionFields;

    @Override
    public void apply(final User user, final String serviceAuthorization) {
        var confirmReadPetitionMigrationEnabled =
            Boolean.parseBoolean(System.getenv().get("ENABLE_CONFIRM_READ_PETITION_MIGRATION"));

        if (confirmReadPetitionMigrationEnabled) {
            log.info("Started migrating cases confirm read petition");
            try {
                ccdSearchService
                    .searchCasesInAwaitingAosWhereConfirmReadPetitionIsYes(user, serviceAuthorization)
                    .parallelStream()
                    .forEach(details -> resetAosFields(details, user, serviceAuthorization));

            } catch (final CcdSearchCaseException e) {
                log.error("Case schedule task (migration confirm read petition) stopped after search error", e);
            }
        }
    }

    private void resetAosFields(final CaseDetails caseDetails, final User user, final String serviceAuthorization) {
        String caseId = caseDetails.getId().toString();
        try {
            ccdUpdateService.submitEventWithRetry(
                caseId,
                SYSTEM_MIGRATE_CASE,
                updateConfirmReadPetitionFields,
                user,
                serviceAuthorization
            );

            log.info("Reset AOS fields successfully for case id: {}", caseId);

        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            log.error("Submit event (after resetting AOS fields) failed for case id: {}, continuing to next case", caseId);
        }
    }
}
