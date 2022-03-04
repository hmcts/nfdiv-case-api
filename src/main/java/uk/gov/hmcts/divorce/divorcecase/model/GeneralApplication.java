package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class GeneralApplication {

    @CCD(
        label = "Choose General Application Type"
    )
    private GeneralApplicationType type;

    @CCD(
        label = "Please provide more information about general application type",
        typeOverride = TextArea
    )
    private String typeOtherComments;

    @CCD(
        label = "Choose General Application Fee Type",
        typeOverride = FixedRadioList,
        typeParameterOverride = "GeneralApplicationFee"
    )
    private GeneralApplicationFee feeType;

    @CCD(
        label = "General Application Document"
    )
    private DivorceDocument document;

    @CCD(
        label = "Additional comments about the supporting document",
        typeOverride = TextArea
    )
    private String documentComments;

    @Builder.Default
    private FeeDetails fee = new FeeDetails();
}
