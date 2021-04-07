package uk.gov.hmcts.reform.divorce.caseapi.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.divorce.caseapi.model.LanguagePreference;

import javax.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "docmosis")
@Validated
@Getter
public class DocmosisTemplatesConfig {
    @NotNull
    private final Map<LanguagePreference, Map<String, String>> templates = new EnumMap<>(LanguagePreference.class);
}
