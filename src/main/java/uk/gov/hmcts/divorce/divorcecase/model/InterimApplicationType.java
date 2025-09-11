package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum InterimApplicationType implements HasLabel {

    @JsonProperty("dispenseWithService")
    DISPENSE_WITH_SERVICE("Dispense with service", AlternativeServiceType.DISPENSED, "hepgor cyflwyno"),

    @JsonProperty("deemedService")
    DEEMED_SERVICE("Deemed service", AlternativeServiceType.DEEMED, "cyflwyno tybiedig"),

    @JsonProperty("alternativeService")
    ALTERNATIVE_SERVICE("Alternative service", AlternativeServiceType.ALTERNATIVE_SERVICE, "cyflwyno amgen"),

    @JsonProperty("bailiffService")
    BAILIFF_SERVICE("Bailiff service", AlternativeServiceType.BAILIFF, "gwasanaeth beili"),

    @JsonProperty("searchGovRecords")
    SEARCH_GOV_RECORDS("Search government records", null, "chwilio cofnodion y llywodraeth"),

    @JsonProperty("processServerService")
    PROCESS_SERVER_SERVICE("Process server service", null, "cyflwyno gan weinyddwr proses");

    private final String label;
    private final AlternativeServiceType serviceType;
    private final String welshLabel;

    public String getLocalizedLabel(boolean isWelsh) {
        return isWelsh ? welshLabel : label;
    }
}
