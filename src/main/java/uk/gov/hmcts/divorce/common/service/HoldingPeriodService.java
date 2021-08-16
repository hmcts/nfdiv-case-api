package uk.gov.hmcts.divorce.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.WEEKS;

@Service
public class HoldingPeriodService {

    @Value("${case_progression.holding_period_in_weeks}")
    private int holdingPeriodInWeeks;

    @Autowired
    private Clock clock;

    public boolean isHoldingPeriodFinished(final LocalDate issueDate) {
        return WEEKS.between(issueDate, now(clock)) >= holdingPeriodInWeeks;
    }

    public int getHoldingPeriodInWeeks() {
        return holdingPeriodInWeeks;
    }

    public LocalDate getDueDateAfter(final LocalDate issueDate) {
        return issueDate.plusWeeks(holdingPeriodInWeeks);
    }
}
