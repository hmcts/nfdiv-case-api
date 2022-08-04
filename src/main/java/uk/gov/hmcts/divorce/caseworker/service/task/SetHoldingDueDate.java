package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SetHoldingDueDate implements CaseTask {

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final CaseData caseData = caseDetails.getData();

        caseData.setDueDate(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()));

        log.info("Setting dueDate of {}, for CaseId: {}, State: Holding", caseData.getDueDate(), caseDetails.getId());

        return caseDetails;
    }
}
