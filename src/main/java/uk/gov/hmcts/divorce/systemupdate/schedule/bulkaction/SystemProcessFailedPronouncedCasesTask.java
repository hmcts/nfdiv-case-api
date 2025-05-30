package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.service.CasePronouncementService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemProcessFailedPronouncedCasesTask implements Runnable {

    private final CcdSearchService ccdSearchService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final CasePronouncementService casePronouncementService;

    @Override
    public void run() {

        log.info("Check bulk case pronounced errors scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        try {

            ccdSearchService
                .searchForUnprocessedOrErroredBulkCases(Pronounced, user, serviceAuth)
                .forEach(caseDetailsBulkCase -> casePronouncementService.systemPronounceCases(caseDetailsBulkCase));

        } catch (final CcdSearchCaseException e) {
            log.error("Retry bulk case pronounced errors schedule task, stopped after search error", e);
        }
    }
}
