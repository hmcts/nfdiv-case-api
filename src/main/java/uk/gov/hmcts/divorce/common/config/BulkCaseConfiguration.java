package uk.gov.hmcts.divorce.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.divorce.bulkaction.task.BulkActionCaseTaskProvider;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Configuration
public class BulkCaseConfiguration {

    @Bean
    public Map<String, BulkActionCaseTaskProvider> bulkActionCaseTaskProviders(final List<BulkActionCaseTaskProvider> providers) {
        return providers.stream()
            .collect(toMap(BulkActionCaseTaskProvider::getEventId, x -> x));
    }
}
