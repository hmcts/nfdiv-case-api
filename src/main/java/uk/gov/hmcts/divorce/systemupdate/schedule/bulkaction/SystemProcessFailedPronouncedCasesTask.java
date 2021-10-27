package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.ErroredBulkCasesService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Component
@Slf4j
public class SystemProcessFailedPronouncedCasesTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ScheduleCaseService scheduleCaseService;

    @Autowired
    private ErroredBulkCasesService erroredBulkCasesService;

    @Override
    public void run() {

        log.info("Check bulk case pronounced errors scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {

            final List<CaseDetails<BulkActionCaseData, BulkActionState>> bulkCases = ccdSearchService
                .searchForUnprocessedOrErroredBulkCases(Pronounced, user, serviceAuth);

            bulkCases.parallelStream()
                .forEach(caseDetailsBulkCase -> erroredBulkCasesService
                    .processErroredCasesAndUpdateBulkCase(
                        caseDetailsBulkCase,
                        SYSTEM_PRONOUNCE_CASE,
                        scheduleCaseService.getCaseTask(caseDetailsBulkCase.getData(), SYSTEM_PRONOUNCE_CASE),
                        user,
                        serviceAuth));

        } catch (final CcdSearchCaseException e) {
            log.error("Retry bulk case pronounced errors schedule task, stopped after search error", e);
        }
    }
}
