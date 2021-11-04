package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Component
public class PronounceCaseProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_PRONOUNCE_CASE;
    }

    @Override
    public CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData) {
        return mainCaseDetails -> {
            final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
            final var finalOrder = mainCaseDetails.getData().getFinalOrder();

            mainCaseDetails.getData().setDueDate(
                finalOrder.getDateFinalOrderEligibleFrom(bulkActionCaseData.getDateAndTimeOfHearing()));
            conditionalOrder.setOutcomeCase(YES);
            conditionalOrder.setGrantedDate(bulkActionCaseData.getDateAndTimeOfHearing().toLocalDate());
            finalOrder.setDateFinalOrderEligibleFrom(
                finalOrder.getDateFinalOrderEligibleFrom(bulkActionCaseData.getDateAndTimeOfHearing()));

            return mainCaseDetails;
        };
    }
}
