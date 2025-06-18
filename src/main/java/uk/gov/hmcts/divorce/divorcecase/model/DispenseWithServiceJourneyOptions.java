package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispenseWithServiceJourneyOptions {

    @CCD(
        label = "Do you and your partner live together?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseLiveTogether;
}
