package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum Court implements HasLabel {

    @JsonProperty("serviceCentre")
    SERVICE_CENTRE("Courts and Tribunals Service Centre", "AA07"),

    @JsonProperty("eastMidlands")
    EAST_MIDLANDS("East Midlands Divorce Unit (Nottingham)", "AA01"),

    @JsonProperty("westMidlands")
    WEST_MIDLANDS("West Midlands Divorce Unit (Stoke)", "AA02"),

    @JsonProperty("southWest")
    SOUTH_WEST("South West Regional Divorce Unit (Southampton)", "AA03"),

    @JsonProperty("northWest")
    NORTH_WEST("North West Regional Divorce Unit (Liverpool)", "AA04"),

    //TODO: What is the Bury St Edmunds Site ID?
    @JsonProperty("buryStEdmunds")
    BURY_ST_EDMUNDS("Bury St Edmunds", "");

    private final String label;
    private final String siteId;
}
