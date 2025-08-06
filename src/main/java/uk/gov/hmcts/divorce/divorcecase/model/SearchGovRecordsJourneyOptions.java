package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.DefaultAccess;

import java.time.LocalDate;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class SearchGovRecordsJourneyOptions implements ApplicationAnswers {

    @CCD(
        label = "Which government departments do you need us to search for your partner's details?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private Set<SearchGovRecordsWhichDepartment> whichDepartments;

    @CCD(
        label = "What have you already done to try to find your partner’s details?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String reasonForApplying;

    @CCD(
        label = "Why do you think these departments are most suited to getting the contact details of your partner?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String whyTheseDepartments;

    @CCD(
        label = "For any other government departments",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String otherDepartmentNames;

    @CCD(
        label = "Enter your partner's name",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2Name;

    @CCD(
        label = "Do you know your partner's date of birth?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo knowApplicant2DateOfBirth;

    @CCD(
        label = "Enter your partner's date of birth",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicant2DateOfBirth;

    @CCD(
        label = "Enter your partner's approximate age",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2ApproximateAge;

    @CCD(
        label = "Do you know your wife's National Insurance number?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String knowApplicant2NationalInsurance;

    @CCD(
        label = "Enter your wife's National Insurance number",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2NationalInsurance;

    @CCD(
        label = "What is your partner's last known address?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2LastKnownAddress;

    @CCD(
        label = "Enter the dates they lived there",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2LastKnownAddressDates;

    @CCD(
        label = "Do you know of any other addresses related to your partner?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String knowApplicant2AdditionalAddresses;

    @CCD(
        label = "Address 1",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2AdditionalAddress1;

    @CCD(
        label = "Enter the dates they lived there",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2AdditionalAddressDates1;

    @CCD(
        label = "Address 2 (optional)",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2AdditionalAddress2;

    @CCD(
        label = "Enter the dates they lived there (optional)",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String applicant2AdditionalAddressDates2;
}
