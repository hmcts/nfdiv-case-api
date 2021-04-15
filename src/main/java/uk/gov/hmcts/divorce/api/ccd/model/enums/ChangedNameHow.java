package uk.gov.hmcts.divorce.api.ccd.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ChangedNameHow implements HasLabel {

    @JsonProperty("marriageCertificate")
    MARRIAGE_CERTIFICATE("Marriage certificate"),

    @JsonProperty("deedPoll")
    DEED_POLL("Deed poll"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
