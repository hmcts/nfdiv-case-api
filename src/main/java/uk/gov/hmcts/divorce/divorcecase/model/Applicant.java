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
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.joinWith;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class Applicant {

    private static final String COMMA_SEPARATOR = ",";
    private static final int ADDRESS_LINE_MAX_CHARS = 25;

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
        label = "Is the language preference Welsh?",
        hint = "Select \"No\" for English or \"Yes\" for bilingual"
    )
    private YesOrNo languagePreferenceWelsh;

    @CCD(
        label = "Did you change your last name when you got married?"
    )
    private YesOrNo lastNameChangedWhenMarried;

    @CCD(
        label = "Have they changed their name since they got married?"
    )
    private YesOrNo nameDifferentToMarriageCertificate;

    @CCD(label = "How did they change their name?")
    private Set<ChangedNameHow> nameChangedHow;

    @CCD(
        label = "Details of how they changed their name",
        typeOverride = TextArea
    )
    private String nameChangedHowOtherDetails;

    @CCD(label = "Address")
    private AddressGlobalUK address;

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

    @CCD(label = "Is represented by a solicitor?")
    private YesOrNo solicitorRepresented;

    @JsonUnwrapped(prefix = "Solicitor")
    private Solicitor solicitor;

    @CCD(
        label = "Does the applicant wish to apply for a financial order?"
    )
    private YesOrNo financialOrder;

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
        label = "PCQ ID"
    )
    private String pcqId;

    @CCD(
        label = "The applicant wants to continue with their application."
    )
    private YesOrNo continueApplication;

    @CCD(label = "Offline")
    @JsonProperty("Offline") // required because isOffline() confuses Jackson
    private YesOrNo offline;

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
        return !isRepresented()
            && nonNull(address)
            && !isBlank(address.getCountry())
            && !("UK").equalsIgnoreCase(address.getCountry())
            && !("United Kingdom").equalsIgnoreCase(address.getCountry());
    }

    @JsonIgnore
    public String getCorrespondenceEmail() {
        return isRepresented() ? solicitor.getEmail() : getEmail();
    }

    @JsonIgnore
    public String getCorrespondenceAddress() {
        if (isRepresented()) {
            return solicitor.getAddress();
        } else if (!isConfidentialContactDetails() && null != address) {
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
        }

        return null;
    }

    @JsonIgnore
    public String getPostalAddress() {
        if (isRepresented()) {
            return solicitor.getAddress();
        }

        if (null != address) {
            String formattedAddressLine1;
            String formattedAddressLine2;
            // Split the string after 25 characters so that it can fit in the address window of envelope
            if (address.getAddressLine1().length() > ADDRESS_LINE_MAX_CHARS) {
                formattedAddressLine1 = substringBefore(address.getAddressLine1(), COMMA_SEPARATOR);
                formattedAddressLine2 = joinWith(
                    ",",
                    substringAfter(address.getAddressLine1(), ","),
                    address.getAddressLine2()
                );
                // remove any space if present due to split
                formattedAddressLine2 = removeStart(formattedAddressLine2, StringUtils.SPACE);
            } else {
                formattedAddressLine1 = address.getAddressLine1();
                formattedAddressLine2 = address.getAddressLine2();
            }

            return Stream.of(
                    formattedAddressLine1,
                    formattedAddressLine2,
                    address.getAddressLine3(),
                    address.getPostTown(),
                    address.getPostCode()
                )
                .filter(value -> value != null && !value.isEmpty())
                .collect(joining("\n"));
        }

        return null;
    }

    @JsonIgnore
    public boolean appliedForFinancialOrder() {
        return nonNull(financialOrder) && financialOrder.toBoolean();
    }

    @JsonIgnore
    public boolean isOffline() {
        return offline != null && offline.toBoolean();
    }

    @JsonIgnore
    public String getFullName() {
        return Stream.of(firstName, middleName, lastName).filter(Objects::nonNull).collect(joining(" "));
    }
}
