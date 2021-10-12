package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SetConfirmServiceDueDate implements CaseTask {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    /**
     * For solicitor service the dueDate is set to 16 days after the date the solicitor says the served the respondent.
     */
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Setting due date.  Case ID: {}", caseDetails.getId());
        final var dueDate = caseDetails.getData().getApplication().getSolicitorService().getDateOfService().plusDays(dueDateOffsetDays);
        caseDetails.getData().setDueDate(dueDate);

        return caseDetails;
    }
}
