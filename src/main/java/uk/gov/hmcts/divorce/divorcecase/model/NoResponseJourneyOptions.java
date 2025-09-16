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
public class NoResponseJourneyOptions {

    @CCD(
        label = "Are these details for your partner correct and up to date?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "NoResponseCheckContactDetails",
        searchable = false
    )
    private NoResponseCheckContactDetails noResponseCheckContactDetails;

    @CCD(
        label = "Can you prove that your partner has received the papers?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo noResponsePartnerHasReceivedPapers;

    @CCD(
        label = "I have a new postal or email address for my partner",
        access = {DefaultAccess.class},
        typeParameterOverride = "NoResponsePartnerNewEmailOrAddress",
        searchable = false
    )
    private NoResponsePartnerNewEmailOrAddress noResponsePartnerNewEmailOrAddress;

    @CCD(
        label = "Provide new email address or apply for alternative service",
        access = {DefaultAccess.class},
        typeParameterOverride = "NoResponseProvidePartnerNewEmailOrAlternativeService",
        searchable = false
    )
    private NoResponseProvidePartnerNewEmailOrAlternativeService noResponseProvidePartnerNewEmailOrAlternativeService;

    @CCD(
        label = "I have a new email address for my partner",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String noResponsePartnerEmailAddress;

    @CCD(
        label = "I have a new postal address for my partner",
        access = {DefaultAccess.class},
        searchable = false
    )
    private AddressGlobalUK noResponsePartnerAddress;

    @CCD(
        label = "Is your partner's new address international?",
        searchable = false
    )
    private YesOrNo noResponsePartnerAddressOverseas;

    @CCD(
        label = "Send papers again or try something else",
        access = {DefaultAccess.class},
        typeParameterOverride = "NoResponseSendPapersAgainOrTrySomethingElse",
        searchable = false
    )
    private NoResponseSendPapersAgainOrTrySomethingElse noResponseSendPapersAgainOrTrySomethingElse;

    @CCD(
        label = "Do you want to apply for in person service, alternative service, or try something else?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private NoResponseNoNewAddressDetails noResponseNoNewAddressDetails;

    @CCD(
        label = "In person service by process server or court bailiff?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private NoResponseProcessServerOrBailiff noResponseProcessServerOrBailiff;

    @CCD(
        label = "I confirm that my partner's address is within England and Wales",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo noResponseRespondentAddressInEnglandWales;

    @CCD(
        label = "Have you already tried to find your partner's contact details?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private NoResponseOwnSearches noResponseOwnSearches;

    @CCD(
        label = "Do you think your partner is still in the UK or is receiving UK benefits?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo noResponsePartnerInUkOrReceivingBenefits;

    @CCD(
        label = "Search gov records or apply to dispense with service?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private NoResponseSearchOrDispense noResponseSearchOrDispense;
}
