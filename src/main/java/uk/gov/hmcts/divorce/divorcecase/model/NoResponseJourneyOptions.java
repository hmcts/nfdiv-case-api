package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoResponseJourneyOptions {

    @CCD(
            label = "Are these details for your partner correct and up to date?",
            typeOverride = FixedRadioList,
            typeParameterOverride = "NoResponseCheckContactDetails"
    )
    private NoResponseCheckContactDetails noResponseCheckContactDetails;

    @CCD(
            label = "Can you prove that your partner has received the papers?",
            access = {DefaultAccess.class}
    )
    private YesOrNo noResponsePartnerHasReceivedPapers;

    @CCD(
        label = "Do you want to apply for in person service, alternative service, or try something else?",
        access = {DefaultAccess.class}
    )
    private NoResponseNoNewAddressDetails noResponseNoNewAddressDetails;

    @CCD(
        label = "In person service by process server or court bailiff?",
        access = {DefaultAccess.class}
    )
    private NoResponseProcessServerOrBailiff noResponseProcessServerOrBailiff;

    @CCD(
        label = "Have you already tried to find your partner's contact details?",
        access = {DefaultAccess.class}
    )
    private NoResponseOwnSearches noResponseOwnSearches;
}
