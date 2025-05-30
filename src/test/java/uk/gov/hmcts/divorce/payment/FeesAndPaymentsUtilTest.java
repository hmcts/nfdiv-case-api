package uk.gov.hmcts.divorce.payment;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FeesAndPaymentsUtilTest {

    @Test
    void penceToPoundsShouldReturnValidPoundValue() {
        assertThat(
            FeesAndPaymentsUtil.penceToPounds("1000"),
            is("10"));
    }

    @Test
    void formatAmountShouldReturnValidCurrencyFormat() {
        assertThat(
            FeesAndPaymentsUtil.formatAmount(100.10),
            is("£100.10"));
    }
}
