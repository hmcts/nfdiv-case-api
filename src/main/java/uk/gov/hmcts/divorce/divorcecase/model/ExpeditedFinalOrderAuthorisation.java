package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpeditedFinalOrderAuthorisation {
    @CCD(
        label = "General Order Authorising Expedited Final Order"
    )
    private DivorceGeneralOrder expeditedFinalOrderGeneralOrder;

    @CCD(
        label = "Expedited Final Order Authorising Judge Name"
    )
    private String expeditedFinalOrderJudgeName;
}
