package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseTask;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;

@Service
@Slf4j
@RequiredArgsConstructor
public class BulkCaseProcessingService {

    private final CcdUpdateService ccdUpdateService;

    public void updateBulkCase(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails,
                                final BulkCaseTask bulkCaseTask,
                                final User user,
                                final String serviceAuth) {
        try {
            ccdUpdateService.submitBulkActionEvent(
                bulkCaseTask,
                bulkCaseDetails.getId(),
                SYSTEM_UPDATE_BULK_CASE,
                user,
                serviceAuth
            );
        } catch (final CcdManagementException e) {
            log.error("Update failed for bulk case id {}, event id {} ", bulkCaseDetails.getId(), SYSTEM_UPDATE_BULK_CASE, e);
        }
    }

    public static List<ListValue<BulkListCaseDetails>> getFailedBulkCases(
        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {

        final var bulkCaseId = bulkCaseDetails.getId();
        final var bulkActionCaseData = bulkCaseDetails.getData();
        final var casesToBeRemoved =
            Objects.isNull(bulkActionCaseData.getCasesToBeRemoved()) ? emptyList() : bulkActionCaseData.getCasesToBeRemoved();

        if (isEmpty(bulkActionCaseData.getProcessedCaseDetails())) {
            log.info("Processed cases list is empty hence processing all cases in bulk case with id {} ", bulkCaseId);
            return Optional.ofNullable(bulkActionCaseData.getBulkListCaseDetails())
                .orElse(Collections.emptyList())
                .stream()
                .filter(erroredCase -> !casesToBeRemoved.contains(erroredCase))
                .toList();
        }

        log.info("Processed cases with errors in bulk case with id {} ", bulkCaseId);
        return Optional.ofNullable(bulkActionCaseData.getErroredCaseDetails())
            .orElse(Collections.emptyList())
            .stream()
            .filter(erroredCase -> !casesToBeRemoved.contains(erroredCase))
            .toList();
    }
}
