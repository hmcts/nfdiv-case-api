package uk.gov.hmcts.reform.divorce.ccd.model.enums;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@AllArgsConstructor
public enum LegalProcessEnum implements HasLabel {

    @JsonProperty("divorce")
    DIVORCE("Divorce"),

    @JsonProperty("dissolution")
    DISSOLUTION("Dissolution"),

    @JsonProperty("judicialSeparation")
    JUDICIAL_SEPARATION("Judicial separation");

    private final String label;
}
