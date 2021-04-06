package uk.gov.hmcts.reform.divorce.caseapi.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.divorce.caseapi.model.LanguagePreference;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "uk.gov.notify.email")
@Validated
@Getter
public class EmailTemplatesConfig {
    @NotNull
    private final Map<LanguagePreference, Map<String, String>> templates = new EnumMap<>(LanguagePreference.class);

    @NotNull
    private final Map<String, Map<String, String>> templateVars = new HashMap<>();
}
