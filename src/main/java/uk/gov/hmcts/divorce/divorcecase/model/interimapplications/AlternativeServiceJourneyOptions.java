package uk.gov.hmcts.divorce.divorcecase.model.interimapplications;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class AlternativeServiceJourneyOptions {

    @CCD(
        label = "Why are you applying to send the documents another way?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String altServiceReasonForApplying;

    @CCD(
        label = "How would you like to apply to send the papers?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "AlternativeServiceMethod",
        searchable = false
    )
    private AlternativeServiceMethod altServiceMethod;

    @CCD(
        label = "Email address",
        searchable = false
    )
    private String altServicePartnerEmail;

    @CCD(
        label = "How do you want to send the divorce papers?",
        searchable = false
    )
    private Set<AlternativeServiceDifferentWays> altServiceDifferentWays;

    @CCD(
        label = "Phone number",
        regex = "^[0-9 +().-]{9,}$",
        searchable = false
    )
    private String altServicePartnerPhone;

    @CCD(
        label = "WhatsApp number",
        regex = "^[0-9 +().-]{9,}$",
        searchable = false
    )
    private String altServicePartnerWANum;

    @CCD(
        label = "Details of private message on social media",
        typeOverride = TextArea,
        searchable = false
    )
    private String altServicePartnerSocialDetails;

    @CCD(
        label = "Details of other way of sending the papers",
        typeOverride = TextArea,
        searchable = false
    )
    private String altServicePartnerOtherDetails;

    @CCD(
        label = "Why are you applying to send the documents by such specific method?",
        typeOverride = TextArea,
        searchable = false
    )
    private String altServiceMethodJustification;
}
