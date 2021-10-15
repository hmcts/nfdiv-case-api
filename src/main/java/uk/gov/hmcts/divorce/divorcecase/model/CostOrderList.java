package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum CostOrderList implements HasLabel {

    @JsonProperty("additionalInformation")
    ADDITIONAL_INFO("Additional information / free text order"),

    @JsonProperty("subjectToDetailedAssessment")
    SUBJECT_TO_DETAILED_ASSESSMENT("An amount subject to detailed assessment if not agreed"),

    @JsonProperty("half")
    HALF_COSTS("To pay half the costs"),

    @JsonProperty("all")
    ALL_COSTS("To pay all the costs");

    private final String label;
}
