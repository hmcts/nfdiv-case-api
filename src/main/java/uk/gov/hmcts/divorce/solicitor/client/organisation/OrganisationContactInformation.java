package uk.gov.hmcts.divorce.solicitor.client.organisation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private String addressLine1;

    @CCD(
        label = "Address Line 2"
    )
    private String addressLine2;

    @CCD(
        label = "Address Line 3"
    )
    private String addressLine3;

    @CCD(
        label = "Country"
    )
    private String country;

    @CCD(
        label = "County"
    )
    private String county;

    @CCD(
        label = "Postcode"
    )
    private String postCode;

    @CCD(
        label = "Town City"
    )
    private String townCity;
}
