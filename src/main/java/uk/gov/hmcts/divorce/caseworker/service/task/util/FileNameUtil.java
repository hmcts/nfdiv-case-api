package uk.gov.hmcts.divorce.caseworker.service.task.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;

import static java.time.format.DateTimeFormatter.ofPattern;

public final class FileNameUtil {

    private static final DateTimeFormatter FILE_NAME_DATE_TIME_FORMATTER = ofPattern("yyyy-MM-dd:HH:mm");
    private static final String DELIMITER = "-";

    private FileNameUtil() {
    }

    public static String formatDocumentName(
        final Long caseId,
        final String documentName,
        final LocalDateTime localDateTime
    ) {
        return new StringJoiner(DELIMITER)
            .add(documentName)
            .add(String.valueOf(caseId))
            .add(localDateTime.format(FILE_NAME_DATE_TIME_FORMATTER))
            .toString();
    }

    public static String formatDocumentName(
        final String documentName,
        final LocalDateTime localDateTime
    ) {
        return new StringJoiner(DELIMITER)
            .add(documentName)
            .add(localDateTime.format(FILE_NAME_DATE_TIME_FORMATTER))
            .toString();
    }

    public static String formatDocumentName(
        final Long caseId,
        final String documentName,
        final String applicant1Or2,
        final LocalDateTime localDateTime
    ) {
        return new StringJoiner(DELIMITER)
            .add(documentName)
            .add(String.valueOf(caseId))
            .add(applicant1Or2)
            .add(localDateTime.format(FILE_NAME_DATE_TIME_FORMATTER))
            .toString();
    }
}
