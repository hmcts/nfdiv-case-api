package uk.gov.hmcts.divorce.bulkaction.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;

@Component
@Slf4j
public class PronounceCaseProvider implements BulkActionCaseTaskProvider {

    @Override
    public String getEventId() {
        return SYSTEM_PRONOUNCE_CASE;
    }

    @Override
    public CaseTask getCaseTask(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {

        final BulkActionCaseData bulkActionCaseData = bulkCaseDetails.getData();

        return mainCaseDetails -> {
            final LocalDateTime dateAndTimeOfHearing = bulkActionCaseData.getDateAndTimeOfHearing();

            if (isNull(dateAndTimeOfHearing)) {
                final String message = format(
                    "Bulk Case has no dateAndTimeOfHearing set for Bulk Case Id: %s, while processing Case Id: %s, Event: %s",
                    bulkCaseDetails.getId(),
                    mainCaseDetails.getId(),
                    getEventId());

                log.error(message);
                throw new BulkActionCaseTaskException(message);
            }

            log.info("Updating case data for Case Id: {} Event: {}", mainCaseDetails.getId(), getEventId());

            final var conditionalOrder = mainCaseDetails.getData().getConditionalOrder();
            final var finalOrder = mainCaseDetails.getData().getFinalOrder();
            final LocalDate dateFinalOrderEligibleFrom = finalOrder.getDateFinalOrderEligibleFrom(dateAndTimeOfHearing);

            conditionalOrder.setPronouncementJudge(bulkActionCaseData.getPronouncementJudge());
            conditionalOrder.setOutcomeCase(YES);
            conditionalOrder.setGrantedDate(dateAndTimeOfHearing.toLocalDate());
            if (!mainCaseDetails.getData().isJudicialSeparationCase()) {
                mainCaseDetails.getData().setDueDate(dateFinalOrderEligibleFrom);
                finalOrder.setDateFinalOrderEligibleFrom(dateFinalOrderEligibleFrom);
                finalOrder.setDateFinalOrderNoLongerEligible(
                    finalOrder.calculateDateFinalOrderNoLongerEligible(conditionalOrder.getGrantedDate()));
                finalOrder.setDateFinalOrderEligibleToRespondent(
                    finalOrder.calculateDateFinalOrderEligibleToRespondent());
            }

            return mainCaseDetails;
        };
    }
}
