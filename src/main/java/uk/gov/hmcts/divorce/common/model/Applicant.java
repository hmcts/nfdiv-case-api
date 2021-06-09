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
        access = {DefaultAccess.class},
        label = "Applicant's first name"
    )
    private String firstName;

    @CCD(
        access = {DefaultAccess.class},
        label = "Applicant's middle name(s)"
    )
    private String middleName;

    @CCD(
        access = {DefaultAccess.class},
        label = "Applicant's last name"
    )
    private String lastName;

    @CCD(
        access = {DefaultAccess.class},
        label = "Applicant's email address",
        typeOverride = Email
    )
    private String email;

    @CCD(
        access = {DefaultAccess.class},
        label = "The applicant has agreed to receive notifications and be served (delivered) court documents by email"
    )
    private YesOrNo agreedToReceiveEmails;

    @CCD(
        access = {DefaultAccess.class},
        label = "Is the language preference Welsh?",
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        access = {DefaultAccess.class},
        label = "Has the applicant changed their name since they got married?",
        hint = "Is the applicant’s current name different to their married name or the name shown on their "
            + "marriage certificate?"
    )
    private YesOrNo nameDifferentToMarriageCertificate;

    @CCD(
        access = {DefaultAccess.class},
        label = "How did the applicant change their name?"
    )
    private ChangedNameHow nameChangedHow;

    @CCD(
        access = {DefaultAccess.class},
        label = "Details of how they changed their name",
        typeOverride = TextArea
    )
    private String nameChangedHowOtherDetails;

    @CCD(
        access = {DefaultAccess.class},
        label = "Applicant's home address"
    )
    private AddressGlobalUK homeAddress;

    @CCD(
        access = {DefaultAccess.class},
        label = "Applicant's phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phoneNumber;

    @CCD(
        access = {DefaultAccess.class},
        label = "Keep Applicant's contact details private?",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialAddress"
    )
    private ConfidentialAddress contactDetailsConfidential;

    @CCD(
        access = {DefaultAccess.class},
        label = "Applicant's gender",
        hint = "Applicant’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender"
    )
    private Gender gender;

    @JsonIgnore
    public LanguagePreference getLanguagePreference() {
        return languagePreferenceWelsh == null || languagePreferenceWelsh.equals(YesOrNo.NO)
            ? ENGLISH
            : WELSH;
    }

}
