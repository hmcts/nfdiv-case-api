package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_AWARE_PARTNER_LIVED;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_CHILDREN_OF_FAMILY;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_CHILD_MAINTENANCE_ORDER;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_CHILD_MAINTENANCE_RESULTS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_CONTACTING_EMPLOYER_RESULTS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_CONTACT_FRIENDS_OR_RELATIVES_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_EMPLOYER_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_EMPLOYER_NAME;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_HAVE_PARTNER_EMAIL_ADDRESSES;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_HAVE_PARTNER_PHONE_NUMBERS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_HAVE_SEARCHED_FINAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_HOW_PARTNER_CONTACT_CHILDREN;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_LIVED_TOGETHER_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_LIVED_TOGETHER_DATE;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_LIVE_TOGETHER;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_OTHER_ENQUIRIES;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_CONTACT_WITH_CHILDREN;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_EMAIL_ADDRESSES;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_LAST_CONTACT_CHILDREN;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_LAST_SEEN_DATE;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_LAST_SEEN_DESCRIPTION;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_LAST_SEEN_OVER_TWO_YEARS_AGO;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_OCCUPATION;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_PAST_ADDRESS_1;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_PAST_ADDRESS_2;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_1;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_2;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_PARTNER_PHONE_NUMBERS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_SEARCHING_ONLINE_RESULTS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_TRACING_AGENT_RESULTS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_TRACING_ONLINE_RESULTS;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_TRIED_CONTACTING_EMPLOYER;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_TRIED_SEARCHING_ONLINE;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_TRIED_TRACING_AGENT;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_TRIED_TRACING_ONLINE;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_WHY_NO_CONTACTING_EMPLOYER;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_WHY_NO_FINAL_ORDER_SEARCH;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_WHY_NO_SEARCHING_ONLINE;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_WHY_NO_TRACING_AGENT;
import static uk.gov.hmcts.divorce.document.content.DispenseWithServiceApplicationTemplateContent.DISPENSE_WHY_NO_TRACING_ONLINE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;

@ExtendWith(MockitoExtension.class)
class DispenseWithServiceApplicationTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private DispenseWithServiceApplicationTemplateContent templateContent;

    private Map<String, Object> getBaseEntries() {
        Map<String, Object> expectedEntries = new LinkedHashMap<>();
        expectedEntries.put("ccdCaseReference", formatId(1616591401473378L));
        expectedEntries.put("applicant1FullName", TEST_FIRST_NAME + " " + TEST_MIDDLE_NAME + " " + TEST_LAST_NAME);
        expectedEntries.put("applicant2FullName", TEST_FIRST_NAME);
        expectedEntries.put("divorceOrDissolution", "divorce application");
        expectedEntries.put("serviceApplicationReceivedDate", "1 January 2023");

        return expectedEntries;
    }

    private Map<String, Object> getYesEntries() {
        Map<String, Object> expectedEntries = getBaseEntries();
        expectedEntries.put(DISPENSE_LIVE_TOGETHER, YesOrNo.YES);
        expectedEntries.put(DISPENSE_LIVED_TOGETHER_DATE, "1 January 2022");
        expectedEntries.put(DISPENSE_LIVED_TOGETHER_ADDRESS, getApplicantWithAddress().getAddress());
        expectedEntries.put(DISPENSE_AWARE_PARTNER_LIVED, YesOrNo.YES);
        expectedEntries.put(DISPENSE_PARTNER_PAST_ADDRESS_1, "Past address 1");
        expectedEntries.put(DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_1, "Enquiries 1");
        expectedEntries.put(DISPENSE_PARTNER_PAST_ADDRESS_2, "Past address 2");
        expectedEntries.put(DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_2, "Enquiries 2");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_DATE, "1 February 2023");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_DESCRIPTION, "Last seen description");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_OVER_TWO_YEARS_AGO, YesOrNo.YES);
        expectedEntries.put(DISPENSE_HAVE_SEARCHED_FINAL_ORDER, YesOrNo.YES);
        expectedEntries.put(DISPENSE_HAVE_PARTNER_EMAIL_ADDRESSES, YesOrNo.YES);
        expectedEntries.put(DISPENSE_PARTNER_EMAIL_ADDRESSES, "email addresses");
        expectedEntries.put(DISPENSE_HAVE_PARTNER_PHONE_NUMBERS, YesOrNo.YES);
        expectedEntries.put(DISPENSE_PARTNER_PHONE_NUMBERS, "phone numbers");
        expectedEntries.put(DISPENSE_TRIED_TRACING_AGENT, YesOrNo.YES);
        expectedEntries.put(DISPENSE_TRACING_AGENT_RESULTS, "tracing agent results");
        expectedEntries.put(DISPENSE_TRIED_TRACING_ONLINE, YesOrNo.YES);
        expectedEntries.put(DISPENSE_TRACING_ONLINE_RESULTS, "tracing online results");
        expectedEntries.put(DISPENSE_TRIED_SEARCHING_ONLINE, YesOrNo.YES);
        expectedEntries.put(DISPENSE_SEARCHING_ONLINE_RESULTS, "searching online results");
        expectedEntries.put(DISPENSE_TRIED_CONTACTING_EMPLOYER, YesOrNo.YES);
        expectedEntries.put(DISPENSE_EMPLOYER_NAME, "employer name");
        expectedEntries.put(DISPENSE_EMPLOYER_ADDRESS, "employer address");
        expectedEntries.put(DISPENSE_PARTNER_OCCUPATION, "partner occupation");
        expectedEntries.put(DISPENSE_CONTACTING_EMPLOYER_RESULTS, "contacting employer results");
        expectedEntries.put(DISPENSE_CHILDREN_OF_FAMILY, YesOrNo.YES);
        expectedEntries.put(DISPENSE_PARTNER_CONTACT_WITH_CHILDREN, YesOrNo.YES);
        expectedEntries.put(DISPENSE_HOW_PARTNER_CONTACT_CHILDREN, "how partner contact children");
        expectedEntries.put(DISPENSE_CHILD_MAINTENANCE_ORDER, YesOrNo.YES);
        expectedEntries.put(DISPENSE_CHILD_MAINTENANCE_RESULTS, "maintenance order");
        expectedEntries.put(DISPENSE_CONTACT_FRIENDS_OR_RELATIVES_DETAILS, "friends or relatives details");
        expectedEntries.put(DISPENSE_OTHER_ENQUIRIES, "other enquiries");
        return expectedEntries;
    }

    private Map<String, Object> getPartialNoEntries() {
        Map<String, Object> expectedEntries = getBaseEntries();
        expectedEntries.put(DISPENSE_LIVE_TOGETHER, YesOrNo.NO);
        expectedEntries.put(DISPENSE_AWARE_PARTNER_LIVED, YesOrNo.NO);
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_DATE, "1 February 2023");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_DESCRIPTION, "Last seen description");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_OVER_TWO_YEARS_AGO, YesOrNo.YES);
        expectedEntries.put(DISPENSE_HAVE_SEARCHED_FINAL_ORDER, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_FINAL_ORDER_SEARCH, "reason for no final order search");
        expectedEntries.put(DISPENSE_HAVE_PARTNER_EMAIL_ADDRESSES, YesOrNo.NO);
        expectedEntries.put(DISPENSE_HAVE_PARTNER_PHONE_NUMBERS, YesOrNo.NO);
        expectedEntries.put(DISPENSE_TRIED_TRACING_AGENT, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_TRACING_AGENT, "reason for no tracing agent");
        expectedEntries.put(DISPENSE_TRIED_TRACING_ONLINE, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_TRACING_ONLINE, "reason for no tracing online");
        expectedEntries.put(DISPENSE_TRIED_SEARCHING_ONLINE, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_SEARCHING_ONLINE, "reason for no searching online");
        expectedEntries.put(DISPENSE_TRIED_CONTACTING_EMPLOYER, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_CONTACTING_EMPLOYER, "reason for no contacting employer");
        expectedEntries.put(DISPENSE_CHILDREN_OF_FAMILY, YesOrNo.YES);
        expectedEntries.put(DISPENSE_PARTNER_CONTACT_WITH_CHILDREN, YesOrNo.NO);
        expectedEntries.put(DISPENSE_PARTNER_LAST_CONTACT_CHILDREN, "last contact");
        expectedEntries.put(DISPENSE_CHILD_MAINTENANCE_ORDER, YesOrNo.NO);
        expectedEntries.put(DISPENSE_CONTACT_FRIENDS_OR_RELATIVES_DETAILS, "friends or relatives details");
        expectedEntries.put(DISPENSE_OTHER_ENQUIRIES, "other enquiries");
        return expectedEntries;
    }

    private Map<String, Object> getNoEntries() {
        Map<String, Object> expectedEntries = getBaseEntries();
        expectedEntries.put(DISPENSE_LIVE_TOGETHER, YesOrNo.NO);
        expectedEntries.put(DISPENSE_AWARE_PARTNER_LIVED, YesOrNo.NO);
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_DATE, "1 February 2023");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_DESCRIPTION, "Last seen description");
        expectedEntries.put(DISPENSE_PARTNER_LAST_SEEN_OVER_TWO_YEARS_AGO, YesOrNo.NO);
        expectedEntries.put(DISPENSE_HAVE_PARTNER_EMAIL_ADDRESSES, YesOrNo.NO);
        expectedEntries.put(DISPENSE_HAVE_PARTNER_PHONE_NUMBERS, YesOrNo.NO);
        expectedEntries.put(DISPENSE_TRIED_TRACING_AGENT, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_TRACING_AGENT, "reason for no tracing agent");
        expectedEntries.put(DISPENSE_TRIED_TRACING_ONLINE, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_TRACING_ONLINE, "reason for no tracing online");
        expectedEntries.put(DISPENSE_TRIED_SEARCHING_ONLINE, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_SEARCHING_ONLINE, "reason for no searching online");
        expectedEntries.put(DISPENSE_TRIED_CONTACTING_EMPLOYER, YesOrNo.NO);
        expectedEntries.put(DISPENSE_WHY_NO_CONTACTING_EMPLOYER, "reason for no contacting employer");
        expectedEntries.put(DISPENSE_CHILDREN_OF_FAMILY, YesOrNo.NO);
        expectedEntries.put(DISPENSE_CONTACT_FRIENDS_OR_RELATIVES_DETAILS, "friends or relatives details");
        expectedEntries.put(DISPENSE_OTHER_ENQUIRIES, "other enquiries");
        return expectedEntries;
    }

    @Test
    void shouldReturnYesTemplateContentForEnglish() {
        final CaseData caseData = buildYesTestData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = getYesEntries();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnPartialNoTemplateContentForEnglish() {
        final CaseData caseData = buildPartialNoTestData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = getPartialNoEntries();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    @Test
    void shouldReturnNoTemplateContentForEnglish() {
        final CaseData caseData = buildNoTestData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);

        final Map<String, Object> result = templateContent.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1()
        );

        Map<String, Object> expectedEntries = getNoEntries();

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    private CaseData buildYesTestData() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        caseData.getApplicant1().setInterimApplicationOptions(
            InterimApplicationOptions.builder()
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .dispenseWithServiceJourneyOptions(
                    DispenseWithServiceJourneyOptions.builder()
                        .dispenseLiveTogether(YesOrNo.YES)
                        .dispenseLivedTogetherDate(LocalDate.of(2022, 1, 1))
                        .dispenseLivedTogetherAddress(getApplicantWithAddress().getAddress())
                        .dispenseAwarePartnerLived(YesOrNo.YES)
                        .dispensePartnerPastAddress1("Past address 1")
                        .dispensePartnerPastAddressEnquiries1("Enquiries 1")
                        .dispensePartnerPastAddress2("Past address 2")
                        .dispensePartnerPastAddressEnquiries2("Enquiries 2")
                        .dispensePartnerLastSeenDate(LocalDate.of(2023, 2, 1))
                        .dispensePartnerLastSeenDescription("Last seen description")
                        .dispensePartnerLastSeenOver2YearsAgo(YesOrNo.YES)
                        .dispenseHaveSearchedFinalOrder(YesOrNo.YES)
                        .dispenseHavePartnerEmailAddresses(YesOrNo.YES)
                        .dispensePartnerEmailAddresses("email addresses")
                        .dispenseHavePartnerPhoneNumbers(YesOrNo.YES)
                        .dispensePartnerPhoneNumbers("phone numbers")
                        .dispenseTriedTracingAgent(YesOrNo.YES)
                        .dispenseTracingAgentResults("tracing agent results")
                        .dispenseTriedTracingOnline(YesOrNo.YES)
                        .dispenseTracingOnlineResults("tracing online results")
                        .dispenseTriedSearchingOnline(YesOrNo.YES)
                        .dispenseSearchingOnlineResults("searching online results")
                        .dispenseTriedContactingEmployer(YesOrNo.YES)
                        .dispenseEmployerName("employer name")
                        .dispenseEmployerAddress("employer address")
                        .dispensePartnerOccupation("partner occupation")
                        .dispenseContactingEmployerResults("contacting employer results")
                        .dispenseChildrenOfFamily(YesOrNo.YES)
                        .dispensePartnerContactWithChildren(YesOrNo.YES)
                        .dispenseHowPartnerContactChildren("how partner contact children")
                        .dispenseChildMaintenanceOrder(YesOrNo.YES)
                        .dispenseChildMaintenanceResults("maintenance order")
                        .dispenseContactFriendsOrRelativesDetails("friends or relatives details")
                        .dispenseOtherEnquiries("other enquiries")
                        .build()
                ).build()
        );
        caseData.setAlternativeService(
            AlternativeService.builder()
                .receivedServiceApplicationDate(LocalDate.of(2023, 1, 1))
                .build()
        );

        return caseData;
    }

    private CaseData buildPartialNoTestData() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        caseData.getApplicant1().setInterimApplicationOptions(
            InterimApplicationOptions.builder()
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .dispenseWithServiceJourneyOptions(
                    DispenseWithServiceJourneyOptions.builder()
                        .dispenseLiveTogether(YesOrNo.NO)
                        .dispenseAwarePartnerLived(YesOrNo.NO)
                        .dispensePartnerLastSeenDate(LocalDate.of(2023, 2, 1))
                        .dispensePartnerLastSeenDescription("Last seen description")
                        .dispensePartnerLastSeenOver2YearsAgo(YesOrNo.YES)
                        .dispenseHaveSearchedFinalOrder(YesOrNo.NO)
                        .dispenseWhyNoFinalOrderSearch("reason for no final order search")
                        .dispenseHavePartnerEmailAddresses(YesOrNo.NO)
                        .dispenseHavePartnerPhoneNumbers(YesOrNo.NO)
                        .dispenseTriedTracingAgent(YesOrNo.NO)
                        .dispenseWhyNoTracingAgent("reason for no tracing agent")
                        .dispenseTriedTracingOnline(YesOrNo.NO)
                        .dispenseWhyNoTracingOnline("reason for no tracing online")
                        .dispenseTriedSearchingOnline(YesOrNo.NO)
                        .dispenseWhyNoSearchingOnline("reason for no searching online")
                        .dispenseTriedContactingEmployer(YesOrNo.NO)
                        .dispenseWhyNoContactingEmployer("reason for no contacting employer")
                        .dispenseChildrenOfFamily(YesOrNo.YES)
                        .dispensePartnerContactWithChildren(YesOrNo.NO)
                        .dispensePartnerLastContactChildren("last contact")
                        .dispenseChildMaintenanceOrder(YesOrNo.NO)
                        .dispenseContactFriendsOrRelativesDetails("friends or relatives details")
                        .dispenseOtherEnquiries("other enquiries")
                        .build()
                ).build()
        );
        caseData.setAlternativeService(
            AlternativeService.builder()
                .receivedServiceApplicationDate(LocalDate.of(2023, 1, 1))
                .build()
        );

        return caseData;
    }

    private CaseData buildNoTestData() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).build());
        caseData.getApplicant1().setInterimApplicationOptions(
            InterimApplicationOptions.builder()
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .dispenseWithServiceJourneyOptions(
                    DispenseWithServiceJourneyOptions.builder()
                        .dispenseLiveTogether(YesOrNo.NO)
                        .dispenseAwarePartnerLived(YesOrNo.NO)
                        .dispensePartnerLastSeenDate(LocalDate.of(2023, 2, 1))
                        .dispensePartnerLastSeenDescription("Last seen description")
                        .dispensePartnerLastSeenOver2YearsAgo(YesOrNo.NO)
                        .dispenseHavePartnerEmailAddresses(YesOrNo.NO)
                        .dispenseHavePartnerPhoneNumbers(YesOrNo.NO)
                        .dispenseTriedTracingAgent(YesOrNo.NO)
                        .dispenseWhyNoTracingAgent("reason for no tracing agent")
                        .dispenseTriedTracingOnline(YesOrNo.NO)
                        .dispenseWhyNoTracingOnline("reason for no tracing online")
                        .dispenseTriedSearchingOnline(YesOrNo.NO)
                        .dispenseWhyNoSearchingOnline("reason for no searching online")
                        .dispenseTriedContactingEmployer(YesOrNo.NO)
                        .dispenseWhyNoContactingEmployer("reason for no contacting employer")
                        .dispenseChildrenOfFamily(YesOrNo.NO)
                        .dispenseContactFriendsOrRelativesDetails("friends or relatives details")
                        .dispenseOtherEnquiries("other enquiries")
                        .build()
                ).build()
        );
        caseData.setAlternativeService(
            AlternativeService.builder()
                .receivedServiceApplicationDate(LocalDate.of(2023, 1, 1))
                .build()
        );

        return caseData;
    }
}
