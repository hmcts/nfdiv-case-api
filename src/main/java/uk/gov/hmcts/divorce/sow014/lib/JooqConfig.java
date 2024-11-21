package uk.gov.hmcts.divorce.sow014.lib;

import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {

    @Bean
    DefaultConfigurationCustomizer jooqSettings() {
        return new DefaultConfigurationCustomizer() {
            @Override
            public void customize(DefaultConfiguration configuration) {
                configuration.settings().withExecuteWithOptimisticLocking(true);
            }
        };
    }
}
