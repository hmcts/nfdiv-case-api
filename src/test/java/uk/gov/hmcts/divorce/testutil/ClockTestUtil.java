package uk.gov.hmcts.divorce.testutil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.mockito.Mockito.when;

public final class ClockTestUtil {

    public static final DateTimeFormatter CCD_DATE_TIME_FORMAT = ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static final Instant NOW = Instant.now();
    public static final ZoneId ZONE_ID = ZoneId.systemDefault();

    private ClockTestUtil() {
    }

    public static void setMockClock(final Clock clock) {
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZONE_ID);
    }

    public static LocalDate getExpectedLocalDate() {
        return LocalDate.ofInstant(NOW, ZONE_ID);
    }

    public static LocalDateTime getExpectedLocalDateTime() {
        return LocalDateTime.ofInstant(NOW, ZONE_ID);
    }

    public static String getFormattedExpectedDateTime() {
        return getExpectedLocalDateTime().format(CCD_DATE_TIME_FORMAT);
    }
}
