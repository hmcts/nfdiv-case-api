package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.util.Set;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Applicant {

    @CCD(label = "First name")
    private String firstName;

    @CCD(
        label = "Middle name(s)",
        hint = "If they have a middle name then you must enter it to avoid amendments later."
    )
    private String middleName;

    @CCD(label = "Last name")
    private String lastName;

    @CCD(
        label = "Email address",
        typeOverride = Email
    )
    private String email;

    @CCD(
        label = "They have agreed to receive notifications and be served (delivered) court documents by email"
    )
    private YesOrNo agreedToReceiveEmails;

    @CCD(
        label = "Has the applicant confirmed the receipt?"
    )
    private YesOrNo confirmReceipt;

    @CCD(
        label = "Has the applicant started the process to apply for conditional order?"
    )
    private YesOrNo applyForConditionalOrderStarted;

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
        label = "Have they changed their name since they got married?",
        hint = "Is their current name different to their married name or the name shown on their "
            + "marriage certificate?"
    )
    private YesOrNo nameDifferentToMarriageCertificate;

    @CCD(label = "How did they change their name?")
    private Set<ChangedNameHow> nameChangedHow;

    @CCD(
        label = "Details of how they changed their name",
        typeOverride = TextArea
    )
    private String nameChangedHowOtherDetails;

    @CCD(label = "Home address")
    private AddressGlobalUK homeAddress;

    @CCD(
        label = "Phone number",
        regex = "^[0-9 +().-]{9,}$"
    )
    private String phoneNumber;

    @CCD(
        label = "Gender",
        hint = "Gender is only collected for statistical purposes.",
        typeOverride = FixedRadioList,
        typeParameterOverride = "Gender"
    )
    private Gender gender;

    @CCD(
        label = "Should ${labelContentApplicantOrApplicant1} contact details be kept private?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "ContactDetailsType"
    )
    private ContactDetailsType contactDetailsType;

    @CCD(
        label = "Service address",
        hint = "If they are to be served at their home address, enter the home address here and as the service "
            + "address below"
    )
    private AddressGlobalUK correspondenceAddress;

    @CCD(label = "Is represented by a solicitor?")
    private YesOrNo solicitorRepresented;

    @JsonUnwrapped(prefix = "Solicitor")
    @CCD(access = {DefaultAccess.class})
    private Solicitor solicitor;

    @CCD(
        label = "Do they wish to apply for a financial order?",
        hint = "The court will not start processing your request for a financial order until you submit the separate "
            + "application and pay the fee."
    )
    private YesOrNo financialOrder;

    @CCD(
        label = "Are there any existing or previous court proceedings relating to the ${labelContentMarriageOrCivilPartnership}?"
    )
    private YesOrNo legalProceedings;

    @CCD(
        label = "Provide details of the other legal proceedings",
        hint = "Provide as much information as possible, such as the case number(s); "
            + "the names of the people involved and if the proceedings are ongoing or if they’ve finished.",
        typeOverride = TextArea
    )
    private String legalProceedingsDetails;

    @CCD(
        label = "PCQ ID"
    )
    private String pcqId;

    @JsonIgnore
    public LanguagePreference getLanguagePreference() {
        return languagePreferenceWelsh == null || languagePreferenceWelsh.equals(NO)
            ? ENGLISH
            : WELSH;
    }

    @JsonIgnore
    public boolean isRepresented() {
        return null != solicitor && isNotEmpty(solicitor.getEmail());
    }

    @JsonIgnore
    public boolean isConfidentialContactDetails() {
        return ContactDetailsType.PRIVATE.equals(contactDetailsType);
    }

    @JsonIgnore
    public boolean isBasedOverseas() {
        return !("UK").equalsIgnoreCase(homeAddress.getCountry());
    }

    @JsonIgnore
    public boolean appliedForFinancialOrder() {
        return nonNull(financialOrder) && financialOrder.toBoolean();
    }

    @JsonIgnore
    public Gender getPartnerGender(MarriageFormation marriageFormation) {
        if (OPPOSITE_SEX_COUPLE.equals(marriageFormation)) {
            if (MALE.equals(this.getGender())) {
                return FEMALE;
            } else {
                return MALE;
            }

        } else {
            if (MALE.equals(this.getGender())) {
                return MALE;
            } else {
                return FEMALE;
            }
        }

    }
}
