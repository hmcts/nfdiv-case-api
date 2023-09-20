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
public class FinalOrderAuthorisation {

    @CCD(
        label = "General Order Authorising Overdue Final Order"
    )
    private DivorceGeneralOrder finalOrderGeneralOrder;

    @CCD(
        label = "Overdue Final Order Authorising Judge Name"
    )
    private String finalOrderJudgeName;
}
