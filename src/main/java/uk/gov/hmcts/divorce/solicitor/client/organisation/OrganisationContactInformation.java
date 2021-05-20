package uk.gov.hmcts.divorce.solicitor.client.organisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationContactInformation {
    @CCD(
        label = "Address Line 1"
    )
    @JsonProperty(value = "addressLine1")
    private String addressLine1;

    @CCD(
        label = "Address Line 2"
    )
    @JsonProperty(value = "addressLine2")
    private String addressLine2;

    @CCD(
        label = "Address Line 3"
    )
    @JsonProperty(value = "addressLine3")
    private String addressLine3;

    @CCD(
        label = "Country"
    )
    @JsonProperty(value = "country")
    private String country;

    @CCD(
        label = "County"
    )
    @JsonProperty(value = "county")
    private String county;

    @CCD(
        label = "Postcode"
    )
    @JsonProperty(value = "postCode")
    private String postCode;

    @CCD(
        label = "Town City"
    )
    @JsonProperty(value = "townCity")
    private String townCity;
}
