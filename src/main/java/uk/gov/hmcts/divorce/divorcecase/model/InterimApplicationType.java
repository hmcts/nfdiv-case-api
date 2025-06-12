package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum InterimApplicationType implements HasLabel {

    @JsonProperty("dispenseWithService")
    DISPENSE_WITH_SERVICE("Dispense with service"),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed service"),

    @JsonProperty("alternativeService")
    ALTERNATIVE_SERVICE("Alternative service"),

    @JsonProperty("bailiffService")
    BAILIFF_SERVICE("Bailiff service"),

    @JsonProperty("searchGovRecords")
    SEARCH_GOV_RECORDS("Search government records");

    private final String label;
}
