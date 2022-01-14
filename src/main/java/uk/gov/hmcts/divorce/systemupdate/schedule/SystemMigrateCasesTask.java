package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Map;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;

@Component
@Slf4j
public class SystemMigrateCasesTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run() {
        log.info("Migrate cases scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

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
        try {
            final var data = RetiredFields.migrate(caseDetails.getData());
            verifyData(data, caseDetails.getId());

            caseDetails.setData(data);
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);
            log.info("Migration complete for case id: {}", caseDetails.getId());
        } catch (final CcdConflictException e) {
            log.error("Could not get lock for case id: {}, continuing to next case", caseDetails.getId());
        } catch (final CcdManagementException e) {
            log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());

            caseDetails.setData(Map.of("dataVersion", 0));
            log.info("Setting dataVersion to 0 for case id: {} after failed migration", caseDetails.getId());
            ccdUpdateService.submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, serviceAuthorization);
            log.info("dataVersion set for case id: {}", caseDetails.getId());
        }
    }

    private void verifyData(Map<String, Object> data, Long id) {
        try {
            objectMapper.convertValue(data, CaseData.class);
        } catch (final IllegalArgumentException e) {
            log.info("Migration failed for case id {} due to deserialization error", id);
            log.info("Deserialization error caused by {}", e.getMessage());

            data.put("dataVersion", 0);
        }
    }
}
