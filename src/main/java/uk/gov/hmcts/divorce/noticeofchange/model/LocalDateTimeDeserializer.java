package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {
    private static final long serialVersionUID = 1L;

    protected LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String dateString = jp.readValueAs(String.class);

        DateTimeFormatter formatter =
                (new DateTimeFormatterBuilder()).append(DateTimeFormatter.ISO_DATE_TIME)
                        .optionalStart().appendFraction(ChronoField.MILLI_OF_SECOND,
                        1, 3, true).optionalEnd().toFormatter();
        return LocalDateTime.parse(dateString, formatter);
    }
}
