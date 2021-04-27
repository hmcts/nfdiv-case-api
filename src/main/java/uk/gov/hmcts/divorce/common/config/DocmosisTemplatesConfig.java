package uk.gov.hmcts.divorce.common.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.divorce.common.model.LanguagePreference;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "docmosis")
@Validated
@Getter
public class DocmosisTemplatesConfig {
    @NotNull
    private final Map<LanguagePreference, Map<String, String>> templates = new HashMap<>();
}
