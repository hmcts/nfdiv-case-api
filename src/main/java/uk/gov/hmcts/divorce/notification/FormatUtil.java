package uk.gov.hmcts.divorce.notification;

import java.time.format.DateTimeFormatter;

import static java.lang.String.join;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;

public final class FormatUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("d MMMM yyyy", UK);
    public static final DateTimeFormatter FILE_NAME_DATE_FORMATTER = ofPattern("yyyy-MM-dd", UK);
    public static final DateTimeFormatter TIME_FORMATTER = ofPattern("H:mm a", UK);

    private FormatUtil() {
    }

    public static String formatId(final Long id) {
        return join("-", id.toString().split("(?<=\\G....)"));
    }
}
