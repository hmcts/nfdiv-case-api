package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentsServedHow implements HasLabel {

    @JsonProperty("courtPermitted")
    COURT_PERMITTED("By other means permitted by the court"),

    @JsonProperty("handedTo")
    HANDED_TO("By personally handing it to or leaving it with"),

    @JsonProperty("deliveredTo")
    DELIVERED_TO("By delivering to or leaving at a permitted place"),

    @JsonProperty("postedTo")
    POSTED_TO("By first class post or other service which provides for delivery on the next day");

    private final String label;
}
