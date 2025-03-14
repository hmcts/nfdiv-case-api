package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;
import uk.gov.hmcts.divorce.bulkaction.task.PronounceCasesTask;
import uk.gov.hmcts.divorce.bulkaction.task.RetryPronounceCasesTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CasePronouncementService {

    private final AuthTokenGenerator authTokenGenerator;

    private final CcdUpdateService ccdUpdateService;

    private final IdamService idamService;

    private final PronounceCasesTask pronounceCasesTask;

    private final RetryPronounceCasesTask retryPronounceCasesTask;

    @Async
    public void pronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        pronounceCasesWithFilter(details, pronounceCasesTask);
    }

    public void systemPronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        pronounceCasesWithFilter(details, pronounceCasesTask);
    }

    @Async
    public void retryPronounceCases(final CaseDetails<BulkActionCaseData, BulkActionState> details) {
        pronounceCasesWithFilter(details, retryPronounceCasesTask);
    }

    private void pronounceCasesWithFilter(CaseDetails<BulkActionCaseData, BulkActionState> details,
                                          BulkCaseTask bulkCaseTask
    ) {
        try {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();
            ccdUpdateService.submitBulkActionEvent(
                bulkCaseTask,
                details.getId(),
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {} ", details.getId(), e);
        }
    }
}
