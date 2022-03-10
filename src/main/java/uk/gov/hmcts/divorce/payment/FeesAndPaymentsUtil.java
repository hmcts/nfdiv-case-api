package uk.gov.hmcts.divorce.payment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class FeesAndPaymentsUtil {

    private FeesAndPaymentsUtil() {
    }

    public static String penceToPounds(final String pence) {
        return NumberFormat.getNumberInstance().format(
            new BigDecimal(pence).movePointLeft(2)
        );
    }

    public static String formatAmount(double amount) {
        return NumberFormat
            .getCurrencyInstance(Locale.UK)
            .format(amount);
    }
}
