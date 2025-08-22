package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum InterimApplicationType implements HasLabel {

    @JsonProperty("dispenseWithService")
    DISPENSE_WITH_SERVICE("Dispense with service", AlternativeServiceType.DISPENSED),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed service", AlternativeServiceType.DEEMED),

    @JsonProperty("alternativeService")
    ALTERNATIVE_SERVICE("Alternative service", AlternativeServiceType.ALTERNATIVE_SERVICE),

    @JsonProperty("bailiffService")
    BAILIFF_SERVICE("Bailiff service", AlternativeServiceType.BAILIFF),

    @JsonProperty("searchGovRecords")
    SEARCH_GOV_RECORDS("Search government records", null),

    @JsonProperty("processServerService")
    PROCESS_SERVER_SERVICE("Process server service", null);

    private final String label;
    private final AlternativeServiceType serviceType;
}
