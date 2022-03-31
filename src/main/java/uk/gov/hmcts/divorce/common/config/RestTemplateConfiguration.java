package uk.gov.hmcts.divorce.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    // required as a dependency of document-management-client
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
