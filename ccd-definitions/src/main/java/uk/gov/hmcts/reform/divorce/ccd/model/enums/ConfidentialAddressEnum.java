package uk.gov.hmcts.reform.divorce.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.types.HasLabel;

@Getter
@AllArgsConstructor
public enum ConfidentialAddressEnum implements HasLabel {

    @JsonProperty("share")
    SHARE("-"),

    @JsonProperty("keep")
    KEEP("Confidential Address");

    private final String label;
}
