package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;

@Component
@RequiredArgsConstructor
public class RemoveCasesTask implements BulkCaseTask {

    private final BulkTriggerService bulkTriggerService;

    private final IdamService idamService;

    private final AuthTokenGenerator authTokenGenerator;

    private final BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Override
    public CaseDetails<BulkActionCaseData, BulkActionState> apply(final CaseDetails<BulkActionCaseData, BulkActionState> details) {

        final BulkActionCaseData bulkActionCaseData = details.getData();
        final List<ListValue<BulkListCaseDetails>> casesToRemove = bulkActionCaseData.getCasesToBeRemoved();

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

        return details;
    }
}
