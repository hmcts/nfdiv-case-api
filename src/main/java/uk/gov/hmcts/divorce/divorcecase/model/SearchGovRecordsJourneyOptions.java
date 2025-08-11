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
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

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
        label = "Search gov records application submitted date",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicationSubmittedDate;

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
    private String partnerName;

    @CCD(
        label = "Do you know your partner's date of birth?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo knowPartnerDateOfBirth;

    @CCD(
        label = "Enter your partner's date of birth",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate partnerDateOfBirth;

    @CCD(
        label = "Enter your partner's approximate age",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerApproximateAge;

    @CCD(
        label = "Do you know your wife's National Insurance number?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo knowPartnerNationalInsurance;

    @CCD(
        label = "Enter your wife's National Insurance number",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerNationalInsurance;

    @CCD(
        label = "What is your partner's last known address?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerLastKnownAddress;

    @CCD(
        label = "Enter the dates they lived there",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String partnerLastKnownAddressDates;

    @CCD(
        label = "Do you know of any other addresses related to your partner?",
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

    @CCD(
        label = "Search gov records application answers",
        access = {DefaultAccess.class},
        searchable = false
    )
    private DivorceDocument applicationAnswers;
}
