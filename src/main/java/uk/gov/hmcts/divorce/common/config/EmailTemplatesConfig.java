package uk.gov.hmcts.divorce.common.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "uk.gov.notify.email")
@Validated
@Getter
public class EmailTemplatesConfig {
    @NotNull
    private final Map<LanguagePreference, Map<String, String>> templates = new EnumMap<>(LanguagePreference.class);

    @NotNull
    private final Map<String, String> templateVars = new HashMap<>();
}
