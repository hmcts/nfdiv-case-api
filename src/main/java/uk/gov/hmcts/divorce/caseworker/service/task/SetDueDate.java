package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDate;

@Component
@Slf4j
public class SetDueDate implements CaseTask {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Value("${case_progression.holding_period_in_weeks}")
    private long holdingPeriodInWeeks;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        if (!caseDetails.getData().getApplication().isSolicitorApplication()) {
            log.info("Setting holding due date.  Case ID: {}", caseDetails.getId());
            caseDetails.getData().setDueDate(LocalDate.now(clock).plusWeeks(holdingPeriodInWeeks).plusDays(1));

        } else if (!caseDetails.getData().getApplication().isSolicitorServiceMethod()) {
            log.info("Setting due date.  Case ID: {}", caseDetails.getId());
            caseDetails.getData().setDueDate(LocalDate.now(clock).plusDays(dueDateOffsetDays));
        }

        return caseDetails;
    }
}
