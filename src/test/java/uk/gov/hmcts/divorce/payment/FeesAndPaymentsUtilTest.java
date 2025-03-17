package uk.gov.hmcts.divorce.payment;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FeesAndPaymentsUtilTest {

    @Test
    public void penceToPoundsShouldReturnValidPoundValue() {
        MatcherAssert.assertThat(
            FeesAndPaymentsUtil.penceToPounds("1000"),
            is("10"));
    }

    @Test
    public void formatAmountShouldReturnValidCurrencyFormat() {
        assertThat(
            FeesAndPaymentsUtil.formatAmount(100.10),
            is("£100.10"));
    }
}
