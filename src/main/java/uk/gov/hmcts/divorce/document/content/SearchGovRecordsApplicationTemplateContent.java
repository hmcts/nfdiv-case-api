package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsWhichDepartment;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static uk.gov.hmcts.divorce.document.DocumentConstants.SEARCH_GOV_RECORDS_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchGovRecordsApplicationTemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    public static final String APPLICATION_DATE = "applicationDate";
    public static final String WHY_SEARCH_GOV_RECORDS = "whySearchGovRecords";
    public static final String DEPARTMENTS_TO_SEARCH = "departmentsToSearch";
    public static final String OTHER_DEPARTMENTS_TO_SEARCH = "otherDepartmentsToSearch";
    public static final String OTHER_DEPARTMENTS = "otherDepartments";
    public static final String REASON_WHY_SEARCH_THESE_DEPARTMENTS = "reasonWhySearchTheseDepartments";
    public static final String PARTNER_NAME = "partnerName";
    public static final String KNOW_PARTNER_DATE_OF_BIRTH = "knowPartnerDateOfBirth";
    public static final String PARTNER_DATE_OF_BIRTH = "partnerDateOfBirth";
    public static final String PARTNER_APPROXIMATE_AGE = "partnerApproximateAge";
    public static final String KNOW_PARTNER_NATIONAL_INSURANCE = "knowPartnerNationalInsurance";
    public static final String PARTNER_NATIONAL_INSURANCE = "partnerNationalInsurance";
    public static final String PARTNER_LAST_KNOWN_ADDRESS = "partnerLastKnownAddress";
    public static final String DATES_PARTNER_LIVED_AT_LAST_KNOWN_ADDRESS = "datesPartnerLivedAtLastKnownAddress";
    public static final String KNOW_ADDITIONAL_ADDRESSES_FOR_PARTNER = "knowAdditionalAddressesForPartner";
    public static final String ADDITIONAL_ADDRESS1 = "additionalAddress1";
    public static final String ADDITIONAL_ADDRESS_1_DATES_LIVED_THERE = "additionalAddress1DatesLivedThere";
    public static final String ADDITIONAL_ADDRESS2 = "additionalAddress2";
    public static final String ADDITIONAL_ADDRESS_2_DATES_LIVED_THERE = "additionalAddress2DatesLivedThere";

    public List<String> getSupportedTemplates() {
        return List.of(
            SEARCH_GOV_RECORDS_APPLICATION_TEMPLATE_ID
        );
    }

    public Map<String, Object> getTemplateContent(
        CaseData caseData, Long caseId,
        Applicant applicant, GeneralApplication generalApplication
    ) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        LanguagePreference languagePreference = applicant.getLanguagePreference();
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);

        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put(
            APPLICATION_DATE, dateTimeFormatter.format(generalApplication.getGeneralApplicationReceivedDate().toLocalDate())
        );

        SearchGovRecordsJourneyOptions applicationAnswers = caseData.getApplicant1().getInterimApplicationOptions()
            .getSearchGovRecordsJourneyOptions();
        return searchGovRecordsApplicationContent(templateContent, applicationAnswers, dateTimeFormatter);
    }

    private Map<String, Object> searchGovRecordsApplicationContent(
        Map<String, Object> templateContent,
        SearchGovRecordsJourneyOptions applicationAnswers,
        DateTimeFormatter dateTimeFormatter
    ) {
        templateContent.put(WHY_SEARCH_GOV_RECORDS, applicationAnswers.getReasonForApplying());
        templateContent.put(DEPARTMENTS_TO_SEARCH, applicationAnswers.getWhichDepartments().stream()
            .filter(Predicate.not(SearchGovRecordsWhichDepartment.OTHER::equals))
                .map(SearchGovRecordsWhichDepartment::getLabel).toList().toString());

        if (applicationAnswers.getWhichDepartments()
            .contains(SearchGovRecordsWhichDepartment.OTHER)) {
            templateContent.put(OTHER_DEPARTMENTS_TO_SEARCH, true);
            templateContent.put(OTHER_DEPARTMENTS, applicationAnswers.getOtherDepartmentNames());
        }

        templateContent.put(PARTNER_NAME, applicationAnswers.getPartnerName());
        templateContent.put(REASON_WHY_SEARCH_THESE_DEPARTMENTS, applicationAnswers.getWhyTheseDepartments());
        templateContent.put(KNOW_PARTNER_DATE_OF_BIRTH, applicationAnswers.getKnowPartnerDateOfBirth());

        if (YesOrNo.YES.equals(applicationAnswers.getKnowPartnerDateOfBirth())) {
            templateContent.put(PARTNER_DATE_OF_BIRTH, dateTimeFormatter.format(applicationAnswers.getPartnerDateOfBirth()));
        } else {
            templateContent.put(PARTNER_APPROXIMATE_AGE, applicationAnswers.getPartnerApproximateAge());
        }

        templateContent.put(KNOW_PARTNER_NATIONAL_INSURANCE, applicationAnswers.getKnowPartnerNationalInsurance());

        if (YesOrNo.YES.equals(applicationAnswers.getKnowPartnerNationalInsurance())) {
            templateContent.put(PARTNER_NATIONAL_INSURANCE, applicationAnswers.getPartnerNationalInsurance());
        }

        templateContent.put(PARTNER_LAST_KNOWN_ADDRESS, applicationAnswers.getLastKnownAddress());
        templateContent.put(DATES_PARTNER_LIVED_AT_LAST_KNOWN_ADDRESS, applicationAnswers.getPartnerLastKnownAddressDates());
        templateContent.put(KNOW_ADDITIONAL_ADDRESSES_FOR_PARTNER, applicationAnswers.getKnowPartnerAdditionalAddresses() == YesOrNo.YES);
        templateContent.put(ADDITIONAL_ADDRESS1, applicationAnswers.getPartnerAdditionalAddress1());
        templateContent.put(ADDITIONAL_ADDRESS_1_DATES_LIVED_THERE, applicationAnswers.getPartnerAdditionalAddressDates1());
        templateContent.put(ADDITIONAL_ADDRESS2, applicationAnswers.getPartnerAdditionalAddress2());
        templateContent.put(ADDITIONAL_ADDRESS_2_DATES_LIVED_THERE, applicationAnswers.getPartnerAdditionalAddressDates2());

        return templateContent;
    }
}
