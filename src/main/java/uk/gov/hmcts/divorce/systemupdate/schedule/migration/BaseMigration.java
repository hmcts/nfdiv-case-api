package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class BaseMigration implements Migration {

    public static final int HIGHEST_PRIORITY = 0;
    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Integer getPriority() {
        return HIGHEST_PRIORITY;
    }

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
        final Long caseId = caseDetails.getId();

        try {
            final var data = RetiredFields.migrate(caseDetails.getData());
            verifyData(data, caseId);

            caseDetails.setData(data);
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);
            log.info("Migration complete for case id: {}", caseId);
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseId);
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseId);
            failedMigrationSetVersionToZero(caseDetails, user, serviceAuthorization, caseId, e);
        }
    }

    private void failedMigrationSetVersionToZero(final CaseDetails caseDetails,
                                                 final User user,
                                                 final String serviceAuthorization,
                                                 final Long caseId,
                                                 final CcdManagementException ccdManagementException) {

        if (ccdManagementException.getStatus() != NOT_FOUND.value()) {
            log.info("Setting dataVersion to 0 for case id: {} after failed migration", caseDetails.getId());

            caseDetails.setData(Map.of("dataVersion", HIGHEST_PRIORITY));
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);

            log.info("dataVersion set for case id: {}", caseDetails.getId());
        } else {
            log.info("Version not set to 0 case not found for case id: {}", caseId);
        }
    }

    private void verifyData(final Map<String, Object> data, final Long id) {
        try {
            objectMapper.convertValue(data, CaseData.class);
        } catch (final IllegalArgumentException e) {
            log.info("Migration failed for case id {} due to deserialization error", id);
            log.info("Deserialization error caused by {}", e.getMessage());

            data.put("dataVersion", HIGHEST_PRIORITY);
        }
    }
}
