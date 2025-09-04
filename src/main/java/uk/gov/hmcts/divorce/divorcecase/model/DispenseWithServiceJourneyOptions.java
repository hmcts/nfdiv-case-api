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
        label = "Did you and your partner live together?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseLiveTogether;

    @CCD(
        label = "Date when you last lived together",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseLivedTogetherDate;

    @CCD(
        label = "Where did you last live together?",
        access = {DefaultAccess.class}
    )
    private AddressGlobalUK dispenseLivedTogetherAddress;

    @CCD(label = "Was this an international address?")
    private YesOrNo dispenseLivedTogetherAddressOverseas;

    @CCD(
        label = "Are you aware of where your partner lived after parting?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseAwarePartnerLived;

    @CCD(
        label = "Where did your partner live after you parted?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddress1;

    @CCD(
        label = "Results of any enquiries made about this address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddressEnquiries1;

    @CCD(
        label = "Where did your partner live after you parted?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddress2;

    @CCD(
        label = "Results of any enquiries made about this address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPastAddressEnquiries2;

    @CCD(
        label = "When was your partner last seen or heard of?",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispensePartnerLastSeenDate;

    @CCD(
        label = "Describe the time you last saw or heard of your partner",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerLastSeenDescription;

    @CCD(
        label = "Is the last seen date more than 2 years in the past at the point that it was provided?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispensePartnerLastSeenOver2YearsAgo;

    @CCD(
        label = "Do you have any email addresses for your partner?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseHavePartnerEmailAddresses;

    @CCD(
        label = "Have you searched for a decree absolute or final order?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseHaveSearchedFinalOrder;

    @CCD(
        label = "Explain why you have not requested a search",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseWhyNoFinalOrderSearch;

    @CCD(
        label = "Tell us the email addresses you have for your partner",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerEmailAddresses;

    @CCD(
        label = "Do you have any phone numbers for your partner?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseHavePartnerPhoneNumbers;

    @CCD(
        label = "Tell us the phone numbers you have for your partner",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerPhoneNumbers;

    @CCD(
        label = "Have you tried using a tracing agent to find your partner?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseTriedTracingAgent;

    @CCD(
        label = "Explain why you have not used a tracing agent",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseWhyNoTracingAgent;

    @CCD(
        label = "What were the results of your tracing agent's search?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseTracingAgentResults;

    @CCD(
        label = "Have you tried tracing your partner online?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseTriedTracingOnline;

    @CCD(
        label = "Explain why you have not tried tracing your partner online",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseWhyNoTracingOnline;

    @CCD(
        label = "What were the results of your online searches?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseTracingOnlineResults;

    @CCD(
        label = "Have you tried finding your partner's details online by searching the internet?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseTriedSearchingOnline;

    @CCD(
        label = "Explain why you have not tried searching for your partner online",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseWhyNoSearchingOnline;

    @CCD(
        label = "What were the results of your online searches?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseSearchingOnlineResults;

    @CCD(
        label = "Have you tried contacting your partner's last known employer?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseTriedContactingEmployer;

    @CCD(
        label = "Explain why you have not tried contacting the last known employer",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseWhyNoContactingEmployer;

    @CCD(
        label = "Employer Name",
        access = {DefaultAccess.class}
    )
    private String dispenseEmployerName;

    @CCD(
        label = "Employer Address",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseEmployerAddress;

    @CCD(
        label = "Partner's Occupation",
        access = {DefaultAccess.class}
    )
    private String dispensePartnerOccupation;

    @CCD(
        label = "What were the results of your contact with your partner's last known employer?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseContactingEmployerResults;

    @CCD(
        label = "Are there any children of the family?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseChildrenOfFamily;

    @CCD(
        label = "Does your partner have any contact with them?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispensePartnerContactWithChildren;

    @CCD(
        label = "When and how does your partner have contact with them?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseHowPartnerContactChildren;

    @CCD(
        label = "To the best of your knowledge, when did your partner last have contact with them?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispensePartnerLastContactChildren;

    @CCD(
        label = "Is there a court order or a child Maintenance Service calculation in place for child maintenance?",
        access = {DefaultAccess.class}
    )
    private YesOrNo dispenseChildMaintenanceOrder;

    @CCD(
        label = "Explain the results of any enquiries made to the Child Maintenance service",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseChildMaintenanceResults;

    @CCD(
        label = "Have you been able to contact any of your partner's friends or relatives?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseContactFriendsOrRelativesDetails;

    @CCD(
        label = "What other enquiries have you made, or information do you have concerning the whereabouts of your partner?",
        typeOverride = TextArea,
        access = {DefaultAccess.class}
    )
    private String dispenseOtherEnquiries;
}
