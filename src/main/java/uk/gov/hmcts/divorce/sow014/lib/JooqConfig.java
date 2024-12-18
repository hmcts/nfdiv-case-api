package uk.gov.hmcts.divorce.sow014.lib;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.jooq.conf.RenderNameStyle.AS_IS;

@Configuration
public class JooqConfig {

    @Autowired
    DataSource ds;

    @Bean
    public DSLContext ctx() {
        return DSL.using(ds, SQLDialect.POSTGRES, new Settings().withRenderNameStyle(AS_IS));
    }

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
