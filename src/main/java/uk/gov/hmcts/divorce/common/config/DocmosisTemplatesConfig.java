package uk.gov.hmcts.divorce.common.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "docmosis")
@Validated
@Getter
public class DocmosisTemplatesConfig {
    @NotNull
    private final Map<LanguagePreference, Map<String, String>> templates = new HashMap<>();

    @NotNull
    private final Map<String, String> templateVars = new HashMap<>();
}
