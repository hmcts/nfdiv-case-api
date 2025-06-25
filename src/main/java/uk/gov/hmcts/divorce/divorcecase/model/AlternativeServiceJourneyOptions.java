package uk.gov.hmcts.divorce.divorcecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlternativeServiceJourneyOptions {

    @CCD(
        label = "Why are you applying to send the documents another way?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String alternativeReason;

    @CCD(
        label = "How would you like to apply to send the papers?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "AlternativeOptions"
    )
    private AlternativeOptions alternativeOptions;

    @CCD(
        label = "Email address"
    )
    private String alternativePartnerEmail;

    @CCD(label = "How do you want to send the divorce papers?")
    private Set<AlternativeDifferentWay> alternativeDifferentWays;

    @CCD(
        label = "Phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String alternativePhone;

    @CCD(
        label = "WhatsApp number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String alternativeWhatsApp;

    @CCD(
        label = "Details of private message on social media",
        typeOverride = TextArea
    )
    private String alternativeSocialDetails;

    @CCD(
        label = "Details of other way of sending the papers",
        typeOverride = TextArea
    )
    private String alternativeOtherDetails;

    @CCD(
        label = "Why are you applying to send the documents by such specific method?",
        typeOverride = TextArea
    )
    private String alternativeMethodReason;
}
