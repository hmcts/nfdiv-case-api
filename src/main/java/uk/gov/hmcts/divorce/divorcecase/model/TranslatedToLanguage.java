package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum TranslatedToLanguage implements HasLabel {

    @JsonProperty("translatedToWelsh")
    WELSH("Welsh"),

    @JsonProperty("translatedToEnglish")
    ENGLISH("English");

    private final String label;
}
