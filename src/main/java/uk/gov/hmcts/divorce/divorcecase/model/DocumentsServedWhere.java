package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentsServedWhere implements HasLabel {

    @JsonProperty("otherSpecify")
    OTHER_SPECIFY("Other (please specify)"),

    @JsonProperty("placeOfBusinessOfCompany")
    PLACE_BUSINESS_COMPANY("place of business of the partnership/company/corporation within the jurisdiction with a connection to claim"),

    @JsonProperty("principalOfficeCompany")
    PRINCIPAL_OFFICE_COMPANY("principal office of the company"),

    @JsonProperty("principalOfficeCorporation")
    PRINCIPAL_OFFICE_CORPORATION("principal office of the corporation"),

    @JsonProperty("principalOfficePartnership")
    PRINCIPAL_OFFICE_PARTNERSHIP("principal office of the partnership"),

    @JsonProperty("lastKnownPricipalBusinessPlace")
    LAST_KNOWN_PRINCIPAL_BUSINESS_PLACE("last known principal place of business"),

    @JsonProperty("lastKnownBusinessPlace")
    LAST_KNOWN_BUSINESS_PLACE("last known place of business"),

    @JsonProperty("principalPlaceBusiness")
    PRINCIPAL_PLACE_BUSINESS("principal place of business"),

    @JsonProperty("placeBusiness")
    PLACE_BUSINESS("place of business"),

    @JsonProperty("lastKnownResidence")
    LAST_KNOWN_RESIDENCE("last known residence"),

    @JsonProperty("usualResidence")
    USUAL_RESIDENCE("usual residence");

    private final String label;
}
