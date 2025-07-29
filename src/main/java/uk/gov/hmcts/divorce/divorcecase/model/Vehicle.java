package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
class Vehicle {
    @CCD(label = "Manufacturer and model")
    private String model;

    @CCD(label = "Colour")
    private String colour;

    @CCD(label = "Registration number")
    private String registration;

    @CCD(
        label = "Other details",
        typeOverride = TextArea
    )
    private String otherDetails;
}
