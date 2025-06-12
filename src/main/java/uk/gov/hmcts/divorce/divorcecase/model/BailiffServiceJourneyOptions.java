package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BailiffServiceJourneyOptions {
    @CCD(label = "Enter your partner's name")
    private String bailiffPartnersName;

    @CCD(
        label = "Is your partner currently resident in a refuge?"
    )
    private YesOrNoOrNotKnown bailiffPartnerInARefuge;

    @CCD(
        label = "Do you know your partner's phone number?"
    )
    private YesOrNo bailiffKnowPartnersPhone;

    @CCD(
        label = "Partners phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String bailiffPartnersPhone;
}
