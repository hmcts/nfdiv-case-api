package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.function.Predicate;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemDropEmptyCase.SYSTEM_DROP_EMPTY_CASE;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;

@Component
@Slf4j
public class FailedBulkCaseRemover {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    public void removeFailedCasesFromBulkListCaseDetails(final List<Long> failedCaseIds,
                                                         final CaseDetails<BulkActionCaseData, BulkActionState> caseDetailsBulkCase,
                                                         final User user,
                                                         final String serviceAuth) {

        final Long bulkCaseId = caseDetailsBulkCase.getId();

        if (!isEmpty(failedCaseIds)) {
            log.info(
                "There are failed awaiting pronouncement cases with ids {} for bulk list case with id {} ",
                failedCaseIds,
                bulkCaseId
            );

            final List<ListValue<BulkListCaseDetails>> bulkCaseDetailsListValues =
                removeFailedCasesFromBulkCaseList(failedCaseIds, caseDetailsBulkCase);

            updateBulkCaseToRemoveCases(caseDetailsBulkCase, user, serviceAuth, bulkCaseId);

            if (bulkCaseDetailsListValues.isEmpty()) {
                dropEmptyBulkCase(caseDetailsBulkCase, user, serviceAuth, bulkCaseId);
            }

        } else {
            log.info("No failed awaiting pronouncement cases to remove from bulk list case with id {} ", bulkCaseId);
        }
    }

    private List<ListValue<BulkListCaseDetails>> removeFailedCasesFromBulkCaseList(
        final List<Long> failedCaseIds,
        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetailsBulkCase) {

        final List<ListValue<BulkListCaseDetails>> bulkCaseDetailsListValues = caseDetailsBulkCase.getData().getBulkListCaseDetails();

        final Predicate<ListValue<BulkListCaseDetails>> listValuePredicate = lv -> {
            Long caseId = Long.valueOf(lv.getValue().getCaseReference().getCaseReference());
            return failedCaseIds.contains(caseId);
        };

        bulkCaseDetailsListValues.removeIf(listValuePredicate);
        return bulkCaseDetailsListValues;
    }

    private void updateBulkCaseToRemoveCases(final CaseDetails<BulkActionCaseData, BulkActionState> caseDetailsBulkCase,
                                             final User user,
                                             final String serviceAuth,
                                             final Long bulkCaseId) {
        log.info("Removing failed awaiting pronouncement cases for bulk case id {}", bulkCaseId);

        try {
            ccdUpdateService.submitBulkActionEvent(
                caseDetailsBulkCase,
                SYSTEM_REMOVE_FAILED_CASES,
                user,
                serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Removing failed awaiting pronouncement cases failed for bulk case id {} ", bulkCaseId);
        }
    }

    private void dropEmptyBulkCase(final CaseDetails<BulkActionCaseData, BulkActionState> caseDetailsBulkCase,
                                   final User user,
                                   final String serviceAuth,
                                   final Long bulkCaseId) {

        log.info("Dropping empty bulk case for bulk case id {}", bulkCaseId);

        try {
            ccdUpdateService.submitBulkActionEvent(
                caseDetailsBulkCase,
                SYSTEM_DROP_EMPTY_CASE,
                user,
                serviceAuth);
        } catch (final CcdManagementException e) {
            log.error("Drop empty bulk case failed for bulk case id {} ", bulkCaseId);
        }
    }
}
