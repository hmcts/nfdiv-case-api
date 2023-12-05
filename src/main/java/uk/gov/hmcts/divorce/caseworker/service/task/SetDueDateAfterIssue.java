package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDate;

@Component
@Slf4j
public class SetDueDateAfterIssue implements CaseTask {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private Clock clock;

    /**
     *  Joint applications skip AoS and go straight to 20 week holding.
     *  Sole applications served by the solicitor issue the AoS when they confirm service and the dueDate is not set after issue.
     *  Sole applications served by the court issue the AoS immediately so the dueDate is 16 days.
     */
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Setting due date.  Case ID: {}", caseDetails.getId());

        CaseData caseData = caseDetails.getData();

        if (!caseData.getApplicationType().isSole()) {
            caseData.setDueDate(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()));
        } else if (caseData.getApplication().isSolicitorServiceMethod()) {
            caseData.setDueDate(null);
        } else {
            caseData.setDueDate(LocalDate.now(clock).plusDays(dueDateOffsetDays));
        }

        caseDetails.setData(caseData);
        return caseDetails;
    }
}
