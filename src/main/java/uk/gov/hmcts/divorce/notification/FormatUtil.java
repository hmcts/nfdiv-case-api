package uk.gov.hmcts.divorce.notification;

import java.time.format.DateTimeFormatter;

import static java.lang.String.join;
import static java.time.format.DateTimeFormatter.ofPattern;

public final class FormatUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("d MMMM yyyy");

    private FormatUtil() {
    }

    public static String formatId(final Long id) {
        return join("-", id.toString().split("(?<=\\G....)"));
    }
}
