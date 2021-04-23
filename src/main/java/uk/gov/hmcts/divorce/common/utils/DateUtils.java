package uk.gov.hmcts.divorce.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
public class DateUtils {

    public static class Formats {
        public static final String TIME = "HH:mm";
        /*
         Format of Date stored in CCD, eg: 2010-05-08
         */
        public static final String CCD_DATE = "yyyy-MM-dd";
        /*
         Format of datetime expected by CCD
         */
        public static final String CCD_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        /*
         Format of dates to display to user, eg. 7 July 1999
         */
        public static final String CLIENT_FACING_DATE = "d MMMM yyyy";
        /*
         Format of datetime expected for document names that can be generated multiple times
         */
        public static final String DOCUMENT_DATE_TIME = "yyyy-MM-dd:HH:mm";
    }

    public static class Settings {
        public static String ZONE = "Europe/London";
        public static TimeZone TIME_ZONE = TimeZone.getTimeZone(ZONE);
        public static Locale LOCALE = Locale.UK;
        public static ZoneId ZONE_ID = ZoneId.of(ZONE);
    }

    public static class Formatters {
        public static DateTimeFormatter CCD_DATE = getFormatter(Formats.CCD_DATE);
        public static DateTimeFormatter CCD_DATE_TIME = getFormatter(Formats.CCD_DATE_TIME);
        public static DateTimeFormatter CLIENT_FACING = getFormatter(Formats.CLIENT_FACING_DATE);
        public static DateTimeFormatter DOCUMENT_DATE_TIME = getFormatter(Formats.DOCUMENT_DATE_TIME);
    }

    private DateUtils() {
        // utility class
    }

    public static Instant parseToInstant(String date) {
        Instant instant = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Formats.CCD_DATE, Settings.LOCALE);
            simpleDateFormat.setTimeZone(Settings.TIME_ZONE);
            instant = simpleDateFormat
                .parse(date)
                .toInstant();
        } catch (ParseException e) {
            log.error("failed to parse date to instant ", e);
        }

        return instant;
    }

    public static String formatDate(Date date) {
        return formatDateFromLocalDate(
            date.toInstant()
                .atZone(Settings.ZONE_ID)
                .toLocalDate()
        );
    }

    public static String formatDate(Instant instant) {
        return formatDate(Date.from(instant));
    }

    public static String formatDateWithCustomerFacingFormat(LocalDate date) {
        return date.format(Formatters.CLIENT_FACING);
    }

    public static String formatDateWithCustomerFacingFormat(String date) {
        return formatDateWithCustomerFacingFormat(
            parseToInstant(date)
                .atZone(Settings.ZONE_ID)
                .toLocalDate()
        );
    }

    public static String formatDateTimeForCcd(LocalDateTime dateTime) {
        return dateTime.format(Formatters.CCD_DATE_TIME);
    }

    public static String formatDateTimeForDocument(LocalDateTime dateTime) {
        return dateTime.format(Formatters.DOCUMENT_DATE_TIME);
    }

    public static String formatDateFromLocalDate(LocalDate date) {
        return date.format(Formatters.CCD_DATE);
    }

    public static String formatDateFromDateTime(LocalDateTime dateTime) {
        return formatDateFromLocalDate(dateTime.toLocalDate());
    }

    public static String formatTimeFromDateTime(LocalDateTime dateTime) {
        return dateTime.toLocalTime().format(getFormatter(Formats.TIME));
    }

    public static String formatNullableDate(Date date, String pattern) {
        return DateFormatUtils.format(
            Optional.ofNullable(date).orElse(new Date(0)),
            pattern
        );
    }

    private static DateTimeFormatter getFormatter(String formatPattern) {
        return DateTimeFormatter.ofPattern(formatPattern, Settings.LOCALE);
    }

    public static String format(Date date, String pattern) {
        return DateFormatUtils.format(
            Optional.ofNullable(date).orElse(new Date(0)),
            pattern
        );
    }
}
