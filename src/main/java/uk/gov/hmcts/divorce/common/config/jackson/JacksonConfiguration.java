package uk.gov.hmcts.divorce.common.config.jackson;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static tools.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import static tools.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES;
import static tools.jackson.databind.MapperFeature.INFER_BUILDER_TYPE_BINDINGS;



@Configuration
public class JacksonConfiguration {

    @Primary
    @Bean
    public ObjectMapper getMapper() {
        ObjectMapper mapper = JsonMapper.builder()
            .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
            // CCD SDK AddressGlobalUK uses PascalCase property names (for example AddressLine1),
            // while callback payloads may contain camelCase equivalents (addressLine1).
            // Allow case-insensitive property mapping for compatibility.
            .configure(ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .enable(INFER_BUILDER_TYPE_BINDINGS)
            .build();

        return mapper;
    }
}
