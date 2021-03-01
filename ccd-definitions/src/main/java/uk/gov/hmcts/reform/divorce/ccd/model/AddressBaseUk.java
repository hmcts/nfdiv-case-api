package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.types.CCD;

@Data
@Builder
@AllArgsConstructor
public class AddressBaseUk {

    @JsonProperty("AddressLine1")
    @CCD(
        label = "Address Line 1"
    )
    private String addressLine1;

    @JsonProperty("AddressLine2")
    @CCD(
        label = "Address Line 2"
    )
    private String addressLine2;

    @JsonProperty("AddressLine3")
    @CCD(
        label = "Address Line 3"
    )
    private String addressLine3;

    @JsonProperty("PostCode")
    @CCD(
        label = "Post Code"
    )
    private String postCode;

    @JsonProperty("PostTown")
    @CCD(
        label = "Post Town"
    )
    private String postTown;

    @JsonProperty("County")
    @CCD(
        label = "County"
    )
    private String county;

    @JsonProperty("Country")
    @CCD(
        label = "Country"
    )
    private String country;
}
