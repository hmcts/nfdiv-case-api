package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.WELSH;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Applicant {

    @CCD(
        label = "Applicant's first name",
        access = {DefaultAccess.class}
    )
    private String firstName;

    @CCD(
        label = "Applicant's middle name(s)",
        access = {DefaultAccess.class}
    )
    private String middleName;

    @CCD(
        label = "Applicant's last name",
        access = {DefaultAccess.class}
    )
    private String lastName;

    @CCD(
        label = "Applicant's email address",
        typeOverride = Email,
        access = {DefaultAccess.class}
    )
    private String email;

    @CCD(
        label = "Applicant 1 has agreed to receive notifications and be served (delivered) court documents by email",
        access = {DefaultAccess.class}
    )
    private YesOrNo agreedToReceiveEmails;

    @CCD(
        label = "Is the language preference Welsh?",
        access = {DefaultAccess.class},
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        label = "Has applicant 1 changed their name since they got married?",
        hint = "Is applicant 1’s current name different to their married name or the name shown on their "
            + "marriage certificate?",
        access = {DefaultAccess.class}
    )
    private YesOrNo nameDifferentToMarriageCertificate;

    @CCD(
        label = "How did Applicant 1 change their name?",
        access = {DefaultAccess.class}
    )
    private ChangedNameHow nameChangedHow;

    @CCD(
        label = "Details of how they changed their name",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String nameChangedHowOtherDetails;

    @CCD(
        label = "Applicant's home address",
        access = {DefaultAccess.class}
    )
    private AddressGlobalUK homeAddress;

    @CCD(
        label = "Applicant's phone number",
        regex = "^[0-9 +().-]{9,}$",
        access = {DefaultAccess.class}
    )
    private String phoneNumber;

    @CCD(
        label = "Keep Applicant's contact details private?",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialAddress",
        access = {DefaultAccess.class}
    )
    private ConfidentialAddress contactDetailsConfidential;

    @JsonIgnore
    public LanguagePreference getLanguagePreference() {
        return languagePreferenceWelsh == null || languagePreferenceWelsh.equals(YesOrNo.NO)
            ? ENGLISH
            : WELSH;
    }

}
