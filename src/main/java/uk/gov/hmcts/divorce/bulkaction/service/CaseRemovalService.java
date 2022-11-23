package uk.gov.hmcts.divorce.bulkaction.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;

@Service
@Slf4j
public class CaseRemovalService {

    @Autowired
    private BulkTriggerService bulkTriggerService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Async
    public void removeCases(final CaseDetails<BulkActionCaseData, BulkActionState> details,
                            final List<ListValue<BulkListCaseDetails>> casesToRemove) {

        final BulkActionCaseData bulkActionCaseData = details.getData();

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        final List<ListValue<BulkListCaseDetails>> unprocessedCases =
            bulkTriggerService.bulkTrigger(
                casesToRemove,
                SYSTEM_REMOVE_BULK_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_REMOVE_BULK_CASE),
                user,
                serviceAuth
            );
        bulkActionCaseData.setCasesToBeRemoved(unprocessedCases);

        try {
            ccdUpdateService.submitBulkActionEvent(
                details,
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final FeignException e) {
            log.error("Update failed for bulk case id {} ", details.getId(), e);
        }
    }
}
