package uk.gov.hmcts.divorce.noticeofchange.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime date, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
        DateTimeFormatter dateFormat = DateTimeFormatter.ISO_DATE_TIME;
        String dateString = dateFormat.format(date);
        jsonGenerator.writeString(dateString);
    }
}
