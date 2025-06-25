package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
class Vehicle {
    @CCD(label = "Manufacturer and model")
    private String manufacturerAndModel;

    @CCD(label = "Colour")
    private String colour;

    @CCD(label = "Registration number")
    private String registrationNumber;
}
