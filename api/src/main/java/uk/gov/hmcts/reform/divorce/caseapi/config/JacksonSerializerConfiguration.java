package uk.gov.hmcts.reform.divorce.caseapi.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.time.format.DateTimeFormatter.ofPattern;

@Configuration
public class JacksonSerializerConfiguration {

    private static final String CCD_DATE_FORMAT = "yyyy-MM-dd";
    private static final String CCD_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateSerializer(ofPattern(CCD_DATE_FORMAT)));
            builder.serializers(new LocalDateTimeSerializer(ofPattern(CCD_DATE_TIME_FORMAT)));
        };
    }
}
