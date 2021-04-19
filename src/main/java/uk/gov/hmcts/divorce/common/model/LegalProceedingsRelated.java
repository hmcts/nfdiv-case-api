package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum LegalProceedingsRelated implements HasLabel {

    @JsonProperty("marriage")
    MARRIAGE("Marriage"),

    @JsonProperty("property")
    PROPERTY("Property"),

    @JsonProperty("children")
    CHILDREN("Children");

    private final String label;
}
