package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class DeemedServiceJourneyOptions implements ApplicationAnswers {
    @CCD(
        label = "Provide a statement",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedNoEvidenceStatement;

    @CCD(
        label = "Tell us about your evidence",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String deemedEvidenceDetails;
}
