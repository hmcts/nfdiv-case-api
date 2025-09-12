package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSE_WITH_SERVICE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispenseWithServiceApplicationTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    public static final String DISPENSE_LIVE_TOGETHER = "dispenseLiveTogether";
    public static final String DISPENSE_LIVED_TOGETHER_DATE = "dispenseLivedTogetherDate";
    public static final String DISPENSE_LIVED_TOGETHER_ADDRESS = "dispenseLivedTogetherAddress";
    public static final String DISPENSE_AWARE_PARTNER_LIVED = "dispenseAwarePartnerLived";
    public static final String DISPENSE_PARTNER_PAST_ADDRESS_1 = "dispensePartnerPastAddress1";
    public static final String DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_1 = "dispensePartnerPastAddressEnquiries1";
    public static final String DISPENSE_PARTNER_PAST_ADDRESS_2 = "dispensePartnerPastAddress2";
    public static final String DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_2 = "dispensePartnerPastAddressEnquiries2";
    public static final String DISPENSE_PARTNER_LAST_SEEN_DATE = "dispensePartnerLastSeenDate";
    public static final String DISPENSE_PARTNER_LAST_SEEN_DESCRIPTION = "dispensePartnerLastSeenDescription";
    public static final String DISPENSE_PARTNER_LAST_SEEN_OVER_TWO_YEARS_AGO = "dispensePartnerLastSeenOverTwoYearsAgo";
    public static final String DISPENSE_HAVE_SEARCHED_FINAL_ORDER = "dispenseHaveSearchedFinalOrder";
    public static final String DISPENSE_WHY_NO_FINAL_ORDER_SEARCH = "dispenseWhyNoFinalOrderSearch";
    public static final String DISPENSE_HAVE_PARTNER_EMAIL_ADDRESSES = "dispenseHavePartnerEmailAddresses";
    public static final String DISPENSE_PARTNER_EMAIL_ADDRESSES = "dispensePartnerEmailAddresses";
    public static final String DISPENSE_HAVE_PARTNER_PHONE_NUMBERS = "dispenseHavePartnerPhoneNumbers";
    public static final String DISPENSE_PARTNER_PHONE_NUMBERS = "dispensePartnerPhoneNumbers";
    public static final String DISPENSE_TRIED_TRACING_AGENT = "dispenseTriedTracingAgent";
    public static final String DISPENSE_TRACING_AGENT_RESULTS = "dispenseTracingAgentResults";
    public static final String DISPENSE_WHY_NO_TRACING_AGENT = "dispenseWhyNoTracingAgent";
    public static final String DISPENSE_TRIED_TRACING_ONLINE = "dispenseTriedTracingOnline";
    public static final String DISPENSE_TRACING_ONLINE_RESULTS = "dispenseTracingOnlineResults";
    public static final String DISPENSE_WHY_NO_TRACING_ONLINE = "dispenseWhyNoTracingOnline";
    public static final String DISPENSE_TRIED_SEARCHING_ONLINE = "dispenseTriedSearchingOnline";
    public static final String DISPENSE_SEARCHING_ONLINE_RESULTS = "dispenseSearchingOnlineResults";
    public static final String DISPENSE_WHY_NO_SEARCHING_ONLINE = "dispenseWhyNoSearchingOnline";
    public static final String DISPENSE_TRIED_CONTACTING_EMPLOYER = "dispenseTriedContactingEmployer";
    public static final String DISPENSE_EMPLOYER_NAME = "dispenseEmployerName";
    public static final String DISPENSE_EMPLOYER_ADDRESS = "dispenseEmployerAddress";
    public static final String DISPENSE_PARTNER_OCCUPATION = "dispensePartnerOccupation";
    public static final String DISPENSE_CONTACTING_EMPLOYER_RESULTS = "dispenseContactingEmployerResults";
    public static final String DISPENSE_WHY_NO_CONTACTING_EMPLOYER = "dispenseWhyNoContactingEmployer";
    public static final String DISPENSE_CHILDREN_OF_FAMILY = "dispenseChildrenOfFamily";
    public static final String DISPENSE_PARTNER_CONTACT_WITH_CHILDREN = "dispensePartnerContactWithChildren";
    public static final String DISPENSE_HOW_PARTNER_CONTACT_CHILDREN = "dispenseHowPartnerContactChildren";
    public static final String DISPENSE_PARTNER_LAST_CONTACT_CHILDREN = "dispensePartnerLastContactChildren";
    public static final String DISPENSE_CHILD_MAINTENANCE_ORDER = "dispenseChildMaintenanceOrder";
    public static final String DISPENSE_CHILD_MAINTENANCE_RESULTS = "dispenseChildMaintenanceResults";
    public static final String DISPENSE_CONTACT_FRIENDS_OR_RELATIVES_DETAILS = "dispenseContactFriendsOrRelativesDetails";
    public static final String DISPENSE_OTHER_ENQUIRIES = "dispenseOtherEnquiries";

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            DISPENSE_WITH_SERVICE_APPLICATION_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        LanguagePreference languagePreference = applicant.getLanguagePreference();
        AlternativeService alternativeService = caseData.getAlternativeService();
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);

        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put(
            SERVICE_APPLICATION_RECEIVED_DATE, dateTimeFormatter.format(alternativeService.getReceivedServiceApplicationDate())
        );

        templateContent.put(DIVORCE_OR_DISSOLUTION, docmosisCommonContent.getApplicationType(languagePreference, caseData));

        DispenseWithServiceJourneyOptions applicationAnswers =
            applicant.getInterimApplicationOptions().getDispenseWithServiceJourneyOptions();
        return dispenseWithServiceApplicationContent(templateContent, applicationAnswers, dateTimeFormatter);
    }

    private Map<String, Object> dispenseWithServiceApplicationContent(
        Map<String, Object> templateContent,
        DispenseWithServiceJourneyOptions applicationAnswers,
        DateTimeFormatter dateTimeFormatter
    ) {
        putDispensePartnerDetails(templateContent, applicationAnswers, dateTimeFormatter);
        putDispenseTracingDetails(templateContent, applicationAnswers);
        
        return templateContent;
    }

    private void putDispensePartnerDetails(
        Map<String, Object> templateContent,
        DispenseWithServiceJourneyOptions applicationAnswers,
        DateTimeFormatter dateTimeFormatter
    ) {
        templateContent.put(DISPENSE_LIVE_TOGETHER, applicationAnswers.getDispenseLiveTogether());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseLiveTogether())) {
            templateContent.put(
                DISPENSE_LIVED_TOGETHER_DATE,
                dateTimeFormatter.format(applicationAnswers.getDispenseLivedTogetherDate())
            );
            templateContent.put(DISPENSE_LIVED_TOGETHER_ADDRESS, applicationAnswers.getDispenseLivedTogetherAddress());
        }

        templateContent.put(DISPENSE_AWARE_PARTNER_LIVED, applicationAnswers.getDispenseAwarePartnerLived());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseAwarePartnerLived())) {
            templateContent.put(DISPENSE_PARTNER_PAST_ADDRESS_1, applicationAnswers.getDispensePartnerPastAddress1());
            templateContent.put(DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_1, applicationAnswers.getDispensePartnerPastAddressEnquiries1());
            templateContent.put(DISPENSE_PARTNER_PAST_ADDRESS_2, applicationAnswers.getDispensePartnerPastAddress2());
            templateContent.put(DISPENSE_PARTNER_PAST_ADDRESS_ENQUIRIES_2, applicationAnswers.getDispensePartnerPastAddressEnquiries2());
        }

        if (applicationAnswers.getDispensePartnerLastSeenDate() != null) {
            templateContent.put(
                DISPENSE_PARTNER_LAST_SEEN_DATE,
                dateTimeFormatter.format(applicationAnswers.getDispensePartnerLastSeenDate())
            );
        }
        templateContent.put(DISPENSE_PARTNER_LAST_SEEN_DESCRIPTION, applicationAnswers.getDispensePartnerLastSeenDescription());
        templateContent.put(DISPENSE_PARTNER_LAST_SEEN_OVER_TWO_YEARS_AGO, applicationAnswers.getDispensePartnerLastSeenOver2YearsAgo());

        if (YesOrNo.YES.equals(applicationAnswers.getDispensePartnerLastSeenOver2YearsAgo())) {
            templateContent.put(DISPENSE_HAVE_SEARCHED_FINAL_ORDER, applicationAnswers.getDispenseHaveSearchedFinalOrder());
            if (YesOrNo.NO.equals(applicationAnswers.getDispenseHaveSearchedFinalOrder())) {
                templateContent.put(DISPENSE_WHY_NO_FINAL_ORDER_SEARCH, applicationAnswers.getDispenseWhyNoFinalOrderSearch());
            }
        }

        templateContent.put(DISPENSE_HAVE_PARTNER_EMAIL_ADDRESSES, applicationAnswers.getDispenseHavePartnerEmailAddresses());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseHavePartnerEmailAddresses())) {
            templateContent.put(DISPENSE_PARTNER_EMAIL_ADDRESSES, applicationAnswers.getDispensePartnerEmailAddresses());
        }

        templateContent.put(DISPENSE_HAVE_PARTNER_PHONE_NUMBERS, applicationAnswers.getDispenseHavePartnerPhoneNumbers());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseHavePartnerPhoneNumbers())) {
            templateContent.put(DISPENSE_PARTNER_PHONE_NUMBERS, applicationAnswers.getDispensePartnerPhoneNumbers());
        }
    }

    private void putDispenseTracingDetails(
        Map<String, Object> templateContent,
        DispenseWithServiceJourneyOptions applicationAnswers
    ) {
        templateContent.put(DISPENSE_TRIED_TRACING_AGENT, applicationAnswers.getDispenseTriedTracingAgent());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseTriedTracingAgent())) {
            templateContent.put(DISPENSE_TRACING_AGENT_RESULTS, applicationAnswers.getDispenseTracingAgentResults());
        } else {
            templateContent.put(DISPENSE_WHY_NO_TRACING_AGENT, applicationAnswers.getDispenseWhyNoTracingAgent());
        }

        templateContent.put(DISPENSE_TRIED_TRACING_ONLINE, applicationAnswers.getDispenseTriedTracingOnline());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseTriedTracingOnline())) {
            templateContent.put(DISPENSE_TRACING_ONLINE_RESULTS, applicationAnswers.getDispenseTracingOnlineResults());
        } else {
            templateContent.put(DISPENSE_WHY_NO_TRACING_ONLINE, applicationAnswers.getDispenseWhyNoTracingOnline());
        }

        templateContent.put(DISPENSE_TRIED_SEARCHING_ONLINE, applicationAnswers.getDispenseTriedSearchingOnline());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseTriedSearchingOnline())) {
            templateContent.put(DISPENSE_SEARCHING_ONLINE_RESULTS, applicationAnswers.getDispenseSearchingOnlineResults());
        } else {
            templateContent.put(DISPENSE_WHY_NO_SEARCHING_ONLINE, applicationAnswers.getDispenseWhyNoSearchingOnline());
        }

        templateContent.put(DISPENSE_TRIED_CONTACTING_EMPLOYER, applicationAnswers.getDispenseTriedContactingEmployer());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseTriedContactingEmployer())) {
            templateContent.put(DISPENSE_EMPLOYER_NAME, applicationAnswers.getDispenseEmployerName());
            templateContent.put(DISPENSE_EMPLOYER_ADDRESS, applicationAnswers.getDispenseEmployerAddress());
            templateContent.put(DISPENSE_PARTNER_OCCUPATION, applicationAnswers.getDispensePartnerOccupation());
            templateContent.put(DISPENSE_CONTACTING_EMPLOYER_RESULTS, applicationAnswers.getDispenseContactingEmployerResults());
        } else {
            templateContent.put(DISPENSE_WHY_NO_CONTACTING_EMPLOYER, applicationAnswers.getDispenseWhyNoContactingEmployer());
        }

        templateContent.put(DISPENSE_CHILDREN_OF_FAMILY, applicationAnswers.getDispenseChildrenOfFamily());
        if (YesOrNo.YES.equals(applicationAnswers.getDispenseChildrenOfFamily())) {
            templateContent.put(DISPENSE_PARTNER_CONTACT_WITH_CHILDREN, applicationAnswers.getDispensePartnerContactWithChildren());
            if (YesOrNo.YES.equals(applicationAnswers.getDispensePartnerContactWithChildren())) {
                templateContent.put(DISPENSE_HOW_PARTNER_CONTACT_CHILDREN, applicationAnswers.getDispenseHowPartnerContactChildren());
            } else {
                templateContent.put(DISPENSE_PARTNER_LAST_CONTACT_CHILDREN, applicationAnswers.getDispensePartnerLastContactChildren());
            }

            templateContent.put(DISPENSE_CHILD_MAINTENANCE_ORDER, applicationAnswers.getDispenseChildMaintenanceOrder());
            if (YesOrNo.YES.equals(applicationAnswers.getDispenseChildMaintenanceOrder())) {
                templateContent.put(DISPENSE_CHILD_MAINTENANCE_RESULTS, applicationAnswers.getDispenseChildMaintenanceResults());
            }
        }

        templateContent.put(DISPENSE_CONTACT_FRIENDS_OR_RELATIVES_DETAILS,
            applicationAnswers.getDispenseContactFriendsOrRelativesDetails());
        templateContent.put(DISPENSE_OTHER_ENQUIRIES, applicationAnswers.getDispenseOtherEnquiries());
    }
}
