package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.AcaSystemUserAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.util.AddressUtil.isEnglandOrWales;

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

    @CCD(
        label = "Last name",
        access = {CaseworkerWithCAAAccess.class}
    )
    private String lastName;

    @CCD(
        label = "Confirm your full name"
    )
    private YesOrNo confirmFullName;

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
        label = "Is the language preference Welsh?",
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        label = "Did they change their last name when they got married?"
    )
    private YesOrNo lastNameChangedWhenMarried;

    @CCD(label = "How did they change their last name when they got married?")
    private Set<ChangedNameHow> lastNameChangedWhenMarriedMethod;

    @CCD(
        label = "Details of how they changed their last name when they got married",
        typeOverride = TextArea
    )
    private String lastNameChangedWhenMarriedOtherDetails;

    @CCD(
        label = "Have they changed their name since they got married?"
    )
    private YesOrNo nameDifferentToMarriageCertificate;

    @CCD(label = "How did they change their name since they got married?")
    private Set<ChangedNameHow> nameDifferentToMarriageCertificateMethod;

    @CCD(
        label = "Details of how they changed their name since they got married",
        typeOverride = TextArea
    )
    private String nameDifferentToMarriageCertificateOtherDetails;

    @CCD(label = "How did they change their name?")
    private Set<ChangedNameHow> nameChangedHow;

    @CCD(
        label = "Details of how they changed their name",
        typeOverride = TextArea
    )
    private String nameChangedHowOtherDetails;

    @CCD(label = "Address")
    private AddressGlobalUK address;

    @CCD(label = "Is this an international address?")
    private YesOrNo addressOverseas;

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
        label = "Is represented by a solicitor?",
        access = {AcaSystemUserAccess.class}
    )
    private YesOrNo solicitorRepresented;

    @JsonUnwrapped(prefix = "Solicitor")
    @CCD(access = {AcaSystemUserAccess.class})
    private Solicitor solicitor;

    @CCD(
        label = "Does the applicant wish to apply for a financial order?"
    )
    private YesOrNo financialOrder;

    @CCD(
        label = "Applicant has used the Welsh translation on submission"
    )
    private YesOrNo usedWelshTranslationOnSubmission;

    @CCD(
        label = "Who are the financial orders for?"
    )
    private Set<FinancialOrderFor> financialOrdersFor;

    @CCD(
        label = "Are there any other legal proceedings relating to the ${labelContentMarriageOrCivilPartnership}?"
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
        label = "Provide details of the other legal proceedings(Translated)",
        typeOverride = TextArea
    )
    private String legalProceedingsDetailsTranslated;

    @CCD(
        label = "Translated To?",
        typeOverride = FixedRadioList,
        typeParameterOverride = "TranslatedToLanguage"
    )
    private TranslatedToLanguage legalProceedingsDetailsTranslatedTo;

    @CCD(
        label = "PCQ ID"
    )
    private String pcqId;

    @CCD(
        label = "The applicant wants to continue with their application."
    )
    private YesOrNo continueApplication;

    @CCD(
        label = "Offline",
        access = {AcaSystemUserAccess.class}
    )
    @JsonProperty("Offline") // required because isOffline() confuses Jackson
    private YesOrNo offline;

    @JsonUnwrapped()
    @Builder.Default
    private ApplicantPrayer applicantPrayer = new ApplicantPrayer();

    @CCD(
        label = "CO Pronounced cover letter regenerated",
        access = {DefaultAccess.class}
    )
    private YesOrNo coPronouncedCoverLetterRegenerated;

    @JsonIgnore
    public LanguagePreference getLanguagePreference() {
        return languagePreferenceWelsh == null || languagePreferenceWelsh.equals(NO)
            ? ENGLISH
            : WELSH;
    }

    @JsonIgnore
    public boolean isRepresented() {
        return null != solicitorRepresented && solicitorRepresented.toBoolean();
    }

    @JsonIgnore
    public boolean isConfidentialContactDetails() {
        return ContactDetailsType.PRIVATE.equals(contactDetailsType);
    }

    @JsonIgnore
    public boolean isBasedOverseas() {
        return !isRepresented() && nonNull(address) && !isBlank(address.getCountry()) && !isEnglandOrWales(address);
    }

    @JsonIgnore
    public String getCorrespondenceEmail() {
        return isRepresented() ? solicitor.getEmail() : getEmail();
    }

    @JsonIgnore
    private String getApplicantAddress() {
        if (YES.equals(addressOverseas)) {
            return Stream.of(
                    address.getAddressLine1(),
                    address.getAddressLine2(),
                    address.getAddressLine3(),
                    address.getPostTown(),
                    address.getCounty(),
                    address.getPostCode(),
                    address.getCountry()
                )
                .filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        } else {
            return Stream.of(
                    address.getAddressLine1(),
                    address.getAddressLine2(),
                    address.getAddressLine3(),
                    address.getPostTown(),
                    address.getCounty(),
                    address.getCountry(),
                    address.getPostCode()
                )
                .filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        }
    }

    @JsonIgnore
    public YesOrNo getCorrespondenceAddressIsOverseas() {
        return this.isRepresented() ? this.getSolicitor().getAddressOverseas() : this.addressOverseas;
    }

    @JsonIgnore
    public String getCorrespondenceAddress() {
        if (isRepresented()) {
            return Stream.of(
                    Optional.ofNullable(solicitor.getOrganisationPolicy())
                        .map(OrganisationPolicy::getOrganisation).map(Organisation::getOrganisationName).orElse(null),
                    solicitor.getAddress()
                ).filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        } else if (!isConfidentialContactDetails() && null != address) {
            return getApplicantAddress();
        }
        return null;
    }

    @JsonIgnore
    public String getCorrespondenceAddressWithoutConfidentialCheck() {
        if (isRepresented()) {
            return Stream.of(
                    Optional.ofNullable(solicitor.getOrganisationPolicy())
                        .map(OrganisationPolicy::getOrganisation).map(Organisation::getOrganisationName).orElse(null),
                    solicitor.getAddress()
                ).filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        } else if (null != address) {
            return getApplicantAddress();
        }
        return null;
    }

    @JsonIgnore
    public boolean appliedForFinancialOrder() {
        return nonNull(financialOrder) && financialOrder.toBoolean();
    }

    @JsonIgnore
    public boolean isApplicantOffline() {
        return offline != null && offline.toBoolean();
    }

    @JsonIgnore
    public String getFullName() {
        return Stream.of(firstName, middleName, lastName).filter(Objects::nonNull).collect(joining(" "));
    }
}
