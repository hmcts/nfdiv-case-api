package uk.gov.hmcts.divorce.bulkscan.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum ScannedDocumentType implements HasLabel {

    @JsonProperty("cherished")
    CHERISHED("Cherished"),

    @JsonProperty("coversheet")
    COVERSHEET("Coversheet"),

    @JsonProperty("form")
    FORM("Form"),

    @JsonProperty("other")
    OTHER("Other");

    private final String label;
}
