package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Applicant {

    @CCD(label = "Applicant's first name")
    private String firstName;

    @CCD(label = "Applicant's middle name(s)")
    private String middleName;

    @CCD(label = "Applicant's last name")
    private String lastName;

    @CCD(
        label = "Applicant's email address",
        typeOverride = Email
    )
    private String email;

    @CCD(
        label = "The applicant has agreed to receive notifications and be served (delivered) court documents by email"
    )
    private YesOrNo agreedToReceiveEmails;

    @CCD(
        label = "Is the language preference Welsh?",
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        label = "Did you change your last name when you got married?"
    )
    private YesOrNo lastNameChangedWhenMarried;

    @CCD(
        label = "Has the applicant changed their name since they got married?",
        hint = "Is the applicant’s current name different to their married name or the name shown on their "
            + "marriage certificate?"
    )
    private YesOrNo nameDifferentToMarriageCertificate;

    @CCD(label = "How did the applicant change their name?")
    private Set<ChangedNameHow> nameChangedHow;

    @CCD(
        label = "Details of how they changed their name",
        typeOverride = TextArea
    )
    private String nameChangedHowOtherDetails;

    @CCD(label = "Applicant's home address")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AddressGlobalUK homeAddress;

    @CCD(
        label = "Applicant's phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phoneNumber;

    @CCD(
        label = "Keep Applicant's contact details private?",
        typeOverride = FixedList,
        typeParameterOverride = "ConfidentialAddress"
    )
    private ConfidentialAddress contactDetailsConfidential;

    @CCD(
        label = "Applicant's gender",
        hint = "Applicant’s gender is collected for statistical purposes only.",
        typeOverride = FixedList,
        typeParameterOverride = "Gender"
    )
    private Gender gender;

    @CCD(
        label = "Applicant's service address",
        hint = "If applicant is to be served at their home address, enter the home address here and as the service "
            + "address below"
    )
    private AddressGlobalUK correspondenceAddress;

    @CCD(label = "Is represented by a solicitor?")
    private YesOrNo solicitorRepresented;

    @JsonUnwrapped(prefix = "Solicitor")
    private Solicitor solicitor;

    @CCD(
        label = "Does the applicant wish to apply for a financial order?",
        hint = "The court will not start processing your request for a financial order until you submit the separate "
            + "application and pay the fee."
    )
    private YesOrNo financialOrder;

    @CCD(
        label = "Who is the financial order for?"
    )
    private Set<FinancialOrderFor> financialOrderFor;

    @JsonIgnore
    public LanguagePreference getLanguagePreference() {
        return languagePreferenceWelsh == null || languagePreferenceWelsh.equals(YesOrNo.NO)
            ? ENGLISH
            : WELSH;
    }

    @JsonIgnore
    public boolean isRepresented() {
        return null != solicitor && isNotEmpty(solicitor.getEmail());
    }
}
