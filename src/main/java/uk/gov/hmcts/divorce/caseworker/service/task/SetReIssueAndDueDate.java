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

import static java.time.LocalDate.now;

@Component
@Slf4j
public class SetReIssueAndDueDate implements CaseTask {

    @Value("${aos_pack.due_date_offset_days}")
    private long dueDateOffsetDays;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {
        details.getData().getApplication().setReissueDate(now(clock));
        details.getData().setDueDate(now(clock).plusDays(dueDateOffsetDays));

        return details;
    }
}
