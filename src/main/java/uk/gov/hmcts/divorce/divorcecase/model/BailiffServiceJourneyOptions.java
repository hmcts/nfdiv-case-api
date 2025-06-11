package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BailiffServiceJourneyOptions {
    @CCD(
        label = "Enter your partner's name",
        access = {DefaultAccess.class}
    )
    private String bailiffPartnersName;
}
