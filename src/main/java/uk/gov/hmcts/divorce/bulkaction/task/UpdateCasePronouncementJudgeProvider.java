package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;

@Component
public class UpdateCasePronouncementJudgeProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;
    }

    @Override
    public CaseTask getCaseTask(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {

        final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();

        return mainCaseDetails -> {
            final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
            conditionalOrder.setPronouncementJudge(
                bulkActionCaseData.getPronouncementJudge()
            );
            return mainCaseDetails;
        };
    }
}
