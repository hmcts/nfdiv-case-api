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
import uk.gov.hmcts.divorce.divorcecase.model.access.CitizenAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccessExcludingSolicitor;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;

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

    @CCD(
        label = "Address",
        searchable = false
    )
    private AddressGlobalUK noRespAddressAddress;

    @CCD(
        label = "Is this an international address?",
        searchable = false
    )
    private YesOrNo noRespAddressAddressOverseas;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        searchable = false
    )
    private String noRespAddressEmail;

    @CCD(
        label = "Is the respondent's email address known?",
        searchable = false
    )
    private YesOrNo noRespAddressKnowsEmail;
}
