package uk.gov.hmcts.divorce.notification;

import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.lang.String.join;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;

public final class FormatUtil {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("d MMMM yyyy", UK);
    public static final DateTimeFormatter WELSH_DATE_TIME_FORMATTER = ofPattern("d MMMM yyyy", new Locale("cy", "GB-WLS"));
    public static final DateTimeFormatter FILE_NAME_DATE_FORMATTER = ofPattern("yyyy-MM-dd", UK);
    public static final DateTimeFormatter TIME_FORMATTER = ofPattern("H:mm a", UK);

    private FormatUtil() {
    }

    public static String formatId(final Long id) {
        return join("-", id.toString().split("(?<=\\G....)"));
    }

    public static DateTimeFormatter getDateTimeFormatterForPreferredLanguage(LanguagePreference languagePreference) {
        return ENGLISH.equals(languagePreference) ? DATE_TIME_FORMATTER : WELSH_DATE_TIME_FORMATTER;
    }
}
