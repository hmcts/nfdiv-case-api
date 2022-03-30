package uk.gov.hmcts.divorce.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class HoldingPeriodService {

    @Value("${case_progression.holding_offset_days}")
    private int holdingPeriodInDays;

    @Value("${applicant.response_offset_days}")
    private int respondOffsetInDays;

    @Autowired
    private Clock clock;

    public boolean isHoldingPeriodFinished(final LocalDate issueDate) {
        return DAYS.between(issueDate, now(clock)) >= holdingPeriodInDays;
    }

    public int getHoldingPeriodInDays() {
        return holdingPeriodInDays;
    }

    public LocalDate getDueDateFor(final LocalDate issueDate) {
        return issueDate.plusDays(holdingPeriodInDays);
    }

    public LocalDate getRespondByDateFor(final LocalDate issueDate) {
        return issueDate.plusDays(respondOffsetInDays);
    }
}
