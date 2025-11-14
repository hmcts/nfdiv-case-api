package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class NoRespondentAddressJourneyOptions {

    @CCD(
        label = "Have you been able to find your partners address?",
        searchable = false
    )
    private YesOrNo noRespAddressHasFoundAddress;

    @CCD(
        label = "Do you have a different way to contact your partner?",
        searchable = false
    )
    private YesOrNo noRespAddressHasWayToContact;

    @CCD(
        label = "Would you like to apply for alternative service?",
        searchable = false
    )
    private YesOrNo noRespAddressWillApplyAltService;
}
