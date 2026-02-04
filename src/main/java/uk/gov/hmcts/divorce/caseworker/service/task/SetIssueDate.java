package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import java.time.Clock;
import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class SetIssueDate implements CaseTask {

    private final Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> details) {

        details.getData().getApplication().setIssueDate(LocalDate.now(clock));

        return details;
    }
}
