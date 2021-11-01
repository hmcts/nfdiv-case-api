package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.function.Predicate;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemRemoveFailedCases.SYSTEM_REMOVE_FAILED_CASES;

@Component
@Slf4j
public class FailedBulkCaseRemover {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    public void removeFailedCasesFromBulkListCaseDetails(final List<Long> failedCaseIds,
                                                         final CaseDetails caseDetailsBulkCase,
                                                         final User user,
                                                         final String serviceAuth) {

        if (!isEmpty(failedCaseIds)) {
            log.info(
                "There are failed awaiting pronouncement cases with ids {} for bulk list case with id {} ",
                failedCaseIds,
                caseDetailsBulkCase.getId()
            );

            final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails =
                caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(caseDetailsBulkCase);

            final List<ListValue<BulkListCaseDetails>> bulkCaseDetailsListValues = bulkActionCaseDetails.getData().getBulkListCaseDetails();

            final Predicate<ListValue<BulkListCaseDetails>> listValuePredicate = lv -> {
                Long caseId = Long.valueOf(lv.getValue().getCaseReference().getCaseReference());
                return failedCaseIds.contains(caseId);
            };

            bulkCaseDetailsListValues.removeIf(listValuePredicate);

            try {
                ccdUpdateService.submitBulkActionEvent(
                    bulkActionCaseDetails,
                    SYSTEM_REMOVE_FAILED_CASES,
                    user,
                    serviceAuth);
            } catch (final CcdManagementException e) {
                log.error("Removing failed awaiting pronouncement cases failed for bulk case id {} ", caseDetailsBulkCase.getId());
            }
        } else {
            log.info("No failed awaiting pronouncement cases to remove from bulk list case with id {} ", caseDetailsBulkCase.getId());
        }
    }
}
