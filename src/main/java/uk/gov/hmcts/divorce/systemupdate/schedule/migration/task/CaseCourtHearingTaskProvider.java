package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkActionCaseTaskProvider;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCaseWithCoEGeneration.SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION;

@Slf4j
@Component
public class CaseCourtHearingTaskProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION;
    }

    @Override
    public CaseTask getCaseTask(CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {
        return caseDetails -> {

            log.info("Updating case data for Case Id: {} Event: {}", caseDetails.getId(), SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION);

            final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();

            final var conditionalOrder = caseDetails.getData().getConditionalOrder();
            conditionalOrder.setDateAndTimeOfHearing(
                bulkActionCaseData.getDateAndTimeOfHearing()
            );
            conditionalOrder.setCourt(
                bulkActionCaseData.getCourt()
            );

            return caseDetails;
        };
    }
}
