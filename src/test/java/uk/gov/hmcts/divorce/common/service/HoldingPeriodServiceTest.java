package uk.gov.hmcts.divorce.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;

@ExtendWith(MockitoExtension.class)
class HoldingPeriodServiceTest {

    private static final int HOLDING_PERIOD_DAYS = 141;

    @Mock
    private Clock clock;

    @InjectMocks
    private HoldingPeriodService holdingPeriodService;

    @BeforeEach
    public void setUp() {
        setField(holdingPeriodService, "holdingPeriodInDays", HOLDING_PERIOD_DAYS);
    }

    @Test
    void shouldReturnHoldingPeriodInDays() {
        assertThat(holdingPeriodService.getHoldingPeriodInDays()).isEqualTo(HOLDING_PERIOD_DAYS);
    }

    @Test
    void shouldReturnDueDateForHoldingPeriodFinish() {

        final LocalDate issueDate = getExpectedLocalDate();

        assertThat(holdingPeriodService.getDueDateFor(issueDate))
            .isEqualTo(issueDate.plusDays(HOLDING_PERIOD_DAYS));
    }

    @Test
    void shouldReturnTrueIfDaysBetweenIssueDateAndCurrentDateIsEqualToHoldingPeriod() {

        setMockClock(clock);

        final LocalDate issueDate = getExpectedLocalDate().minusDays(HOLDING_PERIOD_DAYS);

        assertThat(holdingPeriodService.isHoldingPeriodFinished(issueDate)).isTrue();
    }

    @Test
    void shouldReturnTrueIfDaysBetweenIssueDateAndCurrentDateIsGreaterThanHoldingPeriod() {

        setMockClock(clock);

        final LocalDate issueDate = getExpectedLocalDate().minusDays(HOLDING_PERIOD_DAYS);

        assertThat(holdingPeriodService.isHoldingPeriodFinished(issueDate.minusDays(1))).isTrue();
        assertThat(holdingPeriodService.isHoldingPeriodFinished(issueDate.minusWeeks(1))).isTrue();
    }

    @Test
    void shouldReturnFalseIfDaysBetweenIssueDateAndCurrentDateIsLessThanHoldingPeriod() {

        setMockClock(clock);

        final LocalDate issueDate = getExpectedLocalDate().minusDays(HOLDING_PERIOD_DAYS).plusDays(1);

        assertThat(holdingPeriodService.isHoldingPeriodFinished(issueDate.plusDays(1))).isFalse();
        assertThat(holdingPeriodService.isHoldingPeriodFinished(issueDate.plusWeeks(1))).isFalse();
    }
}
