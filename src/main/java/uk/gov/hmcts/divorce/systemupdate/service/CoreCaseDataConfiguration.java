package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.ccd.client.model.SearchCriteria;

class CoreCaseDataConfiguration {
    @Bean
    @Primary
    Decoder feignDecoder(ObjectMapper objectMapper) {
        return new JacksonDecoder(objectMapper);
    }

    @Bean
    SearchCriteria searchCriteria(ObjectMapper objectMapper) {
        return new SearchCriteria(objectMapper);
    }
}

