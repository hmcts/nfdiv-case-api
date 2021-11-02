package uk.gov.hmcts.divorce.bulkaction.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;

@Component
public class BulkCaseCaseTaskFactory {

    public CaseTask getCaseTask(final BulkActionCaseData bulkActionCaseData, final String eventId) {

        switch (eventId) {

            case SYSTEM_UPDATE_CASE_COURT_HEARING:
                return mainCaseDetails -> {
                    final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
                    conditionalOrder.setDateAndTimeOfHearing(
                        bulkActionCaseData.getDateAndTimeOfHearing()
                    );
                    conditionalOrder.setCourtName(
                        bulkActionCaseData.getCourtName()
                    );
                    return mainCaseDetails;
                };

            case SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE:
                return mainCaseDetails -> {
                    final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
                    conditionalOrder.setPronouncementJudge(
                        bulkActionCaseData.getPronouncementJudge()
                    );
                    return mainCaseDetails;
                };

            case SYSTEM_PRONOUNCE_CASE:
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

            default:
                throw new IllegalArgumentException(String.format("Cannot create CaseTask for Event Id: %s", eventId));
        }
    }
}
