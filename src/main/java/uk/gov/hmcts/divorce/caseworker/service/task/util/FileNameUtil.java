package uk.gov.hmcts.divorce.caseworker.service.task.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

import static java.time.format.DateTimeFormatter.ofPattern;

public final class FileNameUtil {

    public static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd:HH:mm");

    private FileNameUtil() {
    }

    public static String formatDocumentName(
        final Long caseId,
        final String documentName,
        final LocalDateTime localDateTime
    ) {
        return new StringJoiner("-")
            .add(documentName)
            .add(String.valueOf(caseId))
            .add(localDateTime.format(FILE_NAME_DATE_TIME_FORMATTER))
            .toString();
    }
}
