package uk.gov.hmcts.divorce.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfiguration {
    // This configuration class is only for main application as for integration tests Mockito doesn't mock on async methods
}
