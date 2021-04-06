package uk.gov.hmcts.reform.divorce.caseapi.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
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
            builder.deserializers(new LocalDateDeserializer(ofPattern(CCD_DATE_FORMAT)));
            builder.deserializers(new LocalDateTimeDeserializer(ofPattern(CCD_DATE_TIME_FORMAT)));
        };
    }

//    @Bean
//    public com.fasterxml.jackson.databind.Module javaTimeModule() {
//        JavaTimeModule module = new JavaTimeModule();
//        module.addSerializer(new LocalDateSerializer(ofPattern(CCD_DATE_FORMAT)));
//        return module;
//    }

//    @Bean
//    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder().serializers(new LocalDateSerializer(ofPattern(CCD_DATE_FORMAT)))
//            .serializationInclusion(JsonInclude.Include.NON_NULL);
//        return new MappingJackson2HttpMessageConverter(builder.build());
//    }

//    @Bean
//    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
//        return new Jackson2ObjectMapperBuilder()
//            .serializers(
//                new LocalDateSerializer(ofPattern(CCD_DATE_FORMAT)),
//                new LocalDateTimeSerializer(ofPattern(CCD_DATE_TIME_FORMAT)));
//    }
}
