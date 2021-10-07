package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum AlternativeServiceMediumType implements HasLabel {
    @JsonProperty("text")
    TEXT("Text"),

    @JsonProperty("email")
    EMAIL("Email"),

    @JsonProperty("socialMedia")
    SOCIAL_MEDIA("Social media"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
