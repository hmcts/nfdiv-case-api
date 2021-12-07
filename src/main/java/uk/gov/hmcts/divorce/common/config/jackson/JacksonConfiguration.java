package uk.gov.hmcts.divorce.common.config.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static com.fasterxml.jackson.databind.MapperFeature.INFER_BUILDER_TYPE_BINDINGS;

@Configuration
public class JacksonConfiguration {

    /**
     * Here for integration with the CFT lib.
     *
     * When running with the CFT lib an ObjectMapper instance is instantiated by CCD.
     *
     * When running without the CFT lib on the classpath (ie. in prod) we need
     * a default instance to which our customisation is applied.
     */
    @Primary
    @ConditionalOnMissingBean
    @Bean(name = "DefaultObjectMapper")
    ObjectMapper defaultMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ObjectMapper getMapper(ObjectMapper mapper) {
        mapper.configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true);
        mapper.enable(INFER_BUILDER_TYPE_BINDINGS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule deserialization = new SimpleModule();
        deserialization.addDeserializer(HasRole.class, new HasRoleDeserializer());
        mapper.registerModule(deserialization);

        JavaTimeModule datetime = new JavaTimeModule();
        datetime.addSerializer(LocalDateSerializer.INSTANCE);
        mapper.registerModule(datetime);

        return mapper;
    }
}
