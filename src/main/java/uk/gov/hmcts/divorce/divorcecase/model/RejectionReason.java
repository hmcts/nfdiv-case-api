package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum RejectionReason implements HasLabel {

    @JsonProperty("other")
    @JsonAlias({"noCriteria", "insufficentDetails"})
    OTHER("Make a free text order"),

    @JsonProperty("noJurisdiction")
    NO_JURISDICTION("Court does not have jurisdiction");

    private final String label;
}
