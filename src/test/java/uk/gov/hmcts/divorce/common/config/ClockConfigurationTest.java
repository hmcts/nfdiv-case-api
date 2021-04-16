package uk.gov.hmcts.divorce.common.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClockConfigurationTest {

    @Test
    void shouldReturnSystemDefaultClock() {

        final Clock clock = new ClockConfiguration().clock();

        assertNotNull(clock);
        assertThat(clock.getZone(), is(ZoneId.systemDefault()));
    }
}
