package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class DispenseWithServiceJourneyOptions implements ApplicationAnswers {

    @CCD(
        label = "Did the applicant and respondent live together at any time during their relationship?",
        hint = "This includes any period they lived together, regardless of how long ago or how short the time was.",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseLiveTogether;

    @CCD(
        label = "Date when they last lived together",
        hint = "If you do not  know the exact date, please provide an approximate date.",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseLivedTogetherDate;

    @CCD(
        label = "Where did the applicant and respondent last live together?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private AddressGlobalUK dispenseLivedTogetherAddress;

    @CCD(
        label = "Is this an international address?",
        searchable = false
    )
    private YesOrNo dispenseLivedTogetherAddressOverseas;

    @CCD(
        label = "Are you aware of where the respondent lived after parting?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseAwarePartnerLived;

    @CCD(
        label = "Address 1",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerPastAddress1;

    @CCD(
        label = "Results of any enquiries made about this address",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerPastAddressEnquiries1;

    @CCD(
        label = "Address 2",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerPastAddress2;

    @CCD(
        label = "Results of any enquiries made about this address",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerPastAddressEnquiries2;

    @CCD(
        label = "When was the respondent last seen or heard of?",
        hint = "If you are not sure about the day you can enter the last day of the month. If you are not sure about the month "
            + "you can enter '12' for December.",
        access = {DefaultAccess.class},
        searchable = false
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispensePartnerLastSeenDate;

    @CCD(
        label = "Describe the time the applicant saw or heard of the respondent",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerLastSeenDescription;

    @CCD(
        label = "Is the last seen date more than 2 years in the past at the point that it was provided?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispensePartnerLastSeenOver2YearsAgo;

    @CCD(
        label = "Do you have any email addresses for the respondent?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseHavePartnerEmailAddresses;

    @CCD(
        label = "Have you searched for an existing decree absolute or final order?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseHaveSearchedFinalOrder;

    @CCD(
        label = "Explain why you have not requested a search",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseWhyNoFinalOrderSearch;

    @CCD(
        label = "Email addresses",
        hint = "Tell us the email addresses and any previous contact with the respondent. Explain the attempts that has been made to"
            + " contact the respondent on these email addresses.",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerEmailAddresses;

    @CCD(
        label = "Do you have any phone numbers for the respondent?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseHavePartnerPhoneNumbers;

    @CCD(
        label = "Telephone numbers",
        hint = "Tell us the phone numbers and any previous contact with the respondent. Explain the attempts that has been made to"
            + " contact the respondent on these phone numbers.",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerPhoneNumbers;

    @CCD(
        label = "Have you tried using a tracing agent to find the respondent?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseTriedTracingAgent;

    @CCD(
        label = "Explain why you have not used a tracing agent",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseWhyNoTracingAgent;

    @CCD(
        label = "What were the results of your tracing agent's search?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseTracingAgentResults;

    @CCD(
        label = "Have you tried tracing the respondent online?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseTriedTracingOnline;

    @CCD(
        label = "Explain why you have not tried tracing the respondent online",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseWhyNoTracingOnline;

    @CCD(
        label = "What were the results of your online searches?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseTracingOnlineResults;

    @CCD(
        label = "Have you tried finding your partner's details online by searching the internet?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseTriedSearchingOnline;

    @CCD(
        label = "Explain why you have not tried searching for the respondent online",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseWhyNoSearchingOnline;

    @CCD(
        label = "What were the results of your online searches?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseSearchingOnlineResults;

    @CCD(
        label = "Have you tried contacting the respondent's last known employer?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseTriedContactingEmployer;

    @CCD(
        label = "Explain why you have not tried contacting the last known employer",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseWhyNoContactingEmployer;

    @CCD(
        label = "Name of employer",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseEmployerName;

    @CCD(
        label = "Address of employer",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseEmployerAddress;

    @CCD(
        label = "Respondent's Occupation",
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerOccupation;

    @CCD(
        label = "Results of your enquiry with the employer?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseContactingEmployerResults;

    @CCD(
        label = "Are there any children of the family?",
        hint = "This includes any children of the applicant and the respondent, and any stepchildren or other children considered as part "
            + "of the family. This does not include foster children.",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseChildrenOfFamily;

    @CCD(
        label = "Does the respondent have any contact with them?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispensePartnerContactWithChildren;

    @CCD(
        label = "When and how does the respondent have contact with them?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseHowPartnerContactChildren;

    @CCD(
        label = "To the best of your knowledge, when did the respondent last have contact with them?",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispensePartnerLastContactChildren;

    @CCD(
        label = "Is there a court order or a Child Maintenance Service calculation in place for child maintenance?",
        access = {DefaultAccess.class},
        searchable = false
    )
    private YesOrNo dispenseChildMaintenanceOrder;

    @CCD(
        label = "Explain the results of any enquiries made to the Child Maintenance Service about the respondent's whereabouts",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseChildMaintenanceResults;

    @CCD(
        label = "Have you been able to contact any of your respondents's friends or relatives?",
        hint = "You should contact any friends or relatives of the respondent that you are able to, including "
            + "children, to ask about the respondent's whereabouts. You should explain that the applicant has "
            + "started ${labelContentUnionType} application, but do not need to tell them any further details. "
            + "Give their name, addresses (if known), their relationship with the respondent, and tell us about "
            + "any enquiries made with them.",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseContactFriendsOrRelativesDetails;

    @CCD(
        label = "What other enquiries have you made, or information do you have concerning the whereabouts of the respondent?",
        hint = "For example, this could include enquiries made of any professional organisations they may be a member "
            + "of. Enter 'none' if you do not have any more information.",
        typeOverride = TextArea,
        access = {DefaultAccess.class},
        searchable = false
    )
    private String dispenseOtherEnquiries;
}
