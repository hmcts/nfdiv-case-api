package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

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
        label = "Do you have a different way to contact your partner?",
        searchable = false
    )
    private YesOrNo noRespAddressWillApplyAltService;
}
