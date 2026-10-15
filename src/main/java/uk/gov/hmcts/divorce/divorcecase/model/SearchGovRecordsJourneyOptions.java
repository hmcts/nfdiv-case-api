package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class SearchGovRecordsJourneyOptions implements ApplicationAnswers {
    @CCD(
        label = "Which government departments do you need us to search for the respondent's details?",
        hint = "Select all that apply",
        access = {DefaultAccess.class},
        searchable = false
    )
    private Set<SearchGovRecordsWhichDepartment> whichDepartments;

    @CCD(
        label = "What have you already done to try to find the respondent’s details?",
        hint = "Tell us about anything you've already done to try to find the respondent's contact details, "
            + "and the result of these actions",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String reasonForApplying;

    @CCD(
        label = "Why do you think these departments are most suited to getting the contact details of the respondent?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String whyTheseDepartments;

    @CCD(
        label = "Please specify which government departments",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String otherDepartmentNames;

    @CCD(
        label = "Enter the respondent's name",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerName;

    @CCD(
        label = "Do you know the respondent's date of birth?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo knowPartnerDateOfBirth;

    @CCD(
        label = "Enter the respondents's date of birth",
        hint = "For example, 23 3 2000",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate partnerDateOfBirth;

    @CCD(
        label = "Enter the respondents's approximate age",
        hint = "For example, 65 years old",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerApproximateAge;

    @CCD(
        label = "Do you know the respondent's National Insurance number?",
        hint = "If you're able to provide the respondent's National Insurance number it will help with the search",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo knowPartnerNationalInsurance;

    @CCD(
        label = "Enter the respondent's National Insurance number",
        hint = "For example, JB 34 66 84 D",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerNationalInsurance;

    @CCD(
        label = "What is the respondent's last known address?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private AddressGlobalUK partnerLastKnownAddress;

    @CCD(
        label = "Enter the dates they lived there",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerLastKnownAddressDates;

    @CCD(
        label = "Do you know of any other addresses related to the respondent?",
        hint = "If you are able to provide any other addresses, it may help with the search",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo knowPartnerAdditionalAddresses;

    @CCD(
        label = "Address 1",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerAdditionalAddress1;

    @CCD(
        label = "Enter the dates they lived there",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerAdditionalAddressDates1;

    @CCD(
        label = "Address 2 (optional)",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerAdditionalAddress2;

    @CCD(
        label = "Enter the dates they lived there (optional)",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerAdditionalAddressDates2;

    @JsonIgnore
    public String getLastKnownAddress() {
        return Stream.of(
                partnerLastKnownAddress.getAddressLine1(),
                partnerLastKnownAddress.getAddressLine2(),
                partnerLastKnownAddress.getAddressLine3(),
                partnerLastKnownAddress.getPostTown(),
                partnerLastKnownAddress.getCounty(),
                partnerLastKnownAddress.getCountry(),
                partnerLastKnownAddress.getPostCode()
            )
            .filter(value -> value != null && !value.isEmpty())
            .collect(joining("\n"));
    }


}
