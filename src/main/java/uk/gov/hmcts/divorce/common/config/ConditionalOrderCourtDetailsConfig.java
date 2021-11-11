package uk.gov.hmcts.divorce.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.divorce.common.ConditionalOrderCourtDetails;

import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Getter
@Component
@ConfigurationProperties(prefix = "conditional-order-court")
@Validated
public class ConditionalOrderCourtDetailsConfig {

    @NotNull
    private final Map<String, ConditionalOrderCourtDetails> locations = new HashMap<>();

    public ConditionalOrderCourtDetails get(final String courtId) {
        return locations.get(courtId);
    }
}
