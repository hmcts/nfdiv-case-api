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
            typeParameterOverride = "NoResponseCheckContactDetails"
    )
    private NoResponseCheckContactDetails noResponseCheckContactDetails;

    @CCD(
            label = "Can you prove that your partner has received the papers?",
            access = {DefaultAccess.class}
    )
    private YesOrNo noResponsePartnerHasReceivedPapers;

    @CCD(
        label = "I have a new postal or email address",
        access = {DefaultAccess.class},
        typeParameterOverride = "NoResponseNewEmailAndPostalAddress"
    )
    private NoResponseNewEmailAndPostalAddress noResponseUpdateEmailAndPostalAddress;

    @CCD(
        label = "I have a new postal or email address",
        access = {DefaultAccess.class},
        typeParameterOverride = "NoResponseProvideNewEmailOrApplyForAlternativeService"
    )
    private NoResponseProvideNewEmailOrApplyForAlternativeService noResponseProvideNewEmailOrApplyForAlternativeService;


    @CCD(
        label = "I have a new email address for my partner",
        access = {DefaultAccess.class}
    )
    private String noResponsePartnerEmail;

    @CCD(
        label = "I have a new postal address for my partner",
        access = {DefaultAccess.class}
    )
    private AddressGlobalUK noResponsePartnerAddress;

    @CCD(label = "Is your partner's new address international?")
    private YesOrNo noResponsePartnerAddressOverseas;
}
