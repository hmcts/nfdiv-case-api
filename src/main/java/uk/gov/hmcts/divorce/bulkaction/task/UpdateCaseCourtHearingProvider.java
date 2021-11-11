package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;

@Component
public class UpdateCaseCourtHearingProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_UPDATE_CASE_COURT_HEARING;
    }

    @Override
    public CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData) {
        return mainCaseDetails -> {
            final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
            conditionalOrder.setDateAndTimeOfHearing(
                bulkActionCaseData.getDateAndTimeOfHearing()
            );
            conditionalOrder.setCourt(
                bulkActionCaseData.getCourt()
            );
            return mainCaseDetails;
        };
    }
}
