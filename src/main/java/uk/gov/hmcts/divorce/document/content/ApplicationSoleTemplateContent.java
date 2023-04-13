package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant2Represented;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;
import uk.gov.hmcts.divorce.notification.FormatUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.join;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MARRIAGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MARRIAGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_APPLIED_HINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_APPLIED_HINT_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_POLICY_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_POLICY_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_POLICY_HINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_POLICY_HINT_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JURISDICTIONS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;

@Component
@Slf4j
public class ApplicationSoleTemplateContent {

    @Autowired
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Autowired
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    public Map<String, Object> apply(final CaseData caseData, final Long caseId) {

        final Map<String, Object> templateContent = new HashMap<>();

        var isJudicialSeparationCase = caseData.isJudicialSeparationCase();
        var divorceOrCivilPartnershipJS = isJudicialSeparationCase ?  "judicial separation" : "separation";

        if (isJudicialSeparationCase) {
            log.info("For ccd case reference {} and type(judicial separation/separation) {} ", caseId,
                    divorceOrCivilPartnershipJS);
        } else {
            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());
        }

        final Application application = caseData.getApplication();
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        LanguagePreference languagePreference = applicant1.getLanguagePreference();

        templateContent.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce from");
            templateContent.put(DIVORCE_OR_DISSOLUTION, "divorce application");
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
                LanguagePreference.WELSH.equals(languagePreference) ? MARRIAGE_CY : MARRIAGE);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce");
        } else {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH);
            templateContent.put(DIVORCE_OR_DISSOLUTION, "application to end your civil partnership");
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
                LanguagePreference.WELSH.equals(languagePreference) ? CIVIL_PARTNERSHIP_CY : CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership");
        }

        templateContent.put(CCD_CASE_REFERENCE, FormatUtil.formatId(caseId));
        if (application.getIssueDate() != null) {
            templateContent.put(ISSUE_DATE, application.getIssueDate().format(FormatUtil.DATE_TIME_FORMATTER));
        }
        templateContent.put(ISSUE_DATE_POPULATED, application.getIssueDate() != null);

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_MIDDLE_NAME, applicant1.getMiddleName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_1_MARRIAGE_NAME, application.getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_1_POSTAL_ADDRESS, applicant1.getCorrespondenceAddress());
        if (!applicant1.isConfidentialContactDetails()) {
            templateContent.put(APPLICANT_1_EMAIL, applicant1.getEmail());
        }
        if (null != applicant1.getFinancialOrder()) {
            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_1, applicant1.getFinancialOrder().toBoolean());
            templateContent.put(APPLICANT_1_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant1));
            templateContent.put(FINANCIAL_ORDER_POLICY_HEADER, FINANCIAL_ORDER_POLICY_HEADER_TEXT);
            templateContent.put(FINANCIAL_ORDER_POLICY_HINT, FINANCIAL_ORDER_POLICY_HINT_TEXT);
            templateContent.put(FINANCIAL_ORDER_APPLIED_HINT, FINANCIAL_ORDER_APPLIED_HINT_TEXT);
        }
        if (null != applicant1.getLegalProceedings()) {
            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_1, applicant1.getLegalProceedings().toBoolean());
            templateContent.put(APPLICANT_1_COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());
        }
        if (null != application.getApplicant1IsApplicant2Represented()
            && application.getApplicant1IsApplicant2Represented() == Applicant2Represented.YES) {
            setSolicitorDetails(templateContent, applicant2);
        }

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_MIDDLE_NAME, applicant2.getMiddleName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(APPLICANT_2_MARRIAGE_NAME, application.getMarriageDetails().getApplicant2Name());

        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);

        applicationTemplateDataProvider.mapMarriageDetails(templateContent, application);

        templateContent.put(JURISDICTIONS, applicationTemplateDataProvider.deriveJurisdictionList(application, caseId,
            applicant1.getLanguagePreference()));

        return templateContent;
    }

    private void setSolicitorDetails(Map<String, Object> templateContent, Applicant applicant) {
        String solicitorName = applicant.getSolicitor().getName();
        String solicitorEmail = applicant.getSolicitor().getEmail();
        String solicitorFirmName = applicant.getSolicitor().getFirmName();
        String solicitorAddress = applicant.getSolicitor().getAddress();
        boolean hasEnteredSolicitorDetails =
            !isNullOrEmpty(solicitorName)
            || !isNullOrEmpty(solicitorEmail)
            || !isNullOrEmpty(solicitorFirmName)
            || !isNullOrEmpty(solicitorAddress)
            && !isNullOrEmpty(solicitorAddress.replace("\n", ""));
        templateContent.put(APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS, hasEnteredSolicitorDetails);
        if (!isNullOrEmpty(solicitorName)) {
            templateContent.put(APPLICANT_2_SOLICITOR_NAME, solicitorName);
        }
        if (!isNullOrEmpty(solicitorEmail)) {
            templateContent.put(APPLICANT_2_SOLICITOR_EMAIL, solicitorEmail);
        }
        if (!isNullOrEmpty(solicitorFirmName)) {
            templateContent.put(APPLICANT_2_SOLICITOR_FIRM_NAME, solicitorFirmName);
        }
        if (!isNullOrEmpty(solicitorAddress)) {
            String addressCleanUp =
                join("\n",
                    Arrays.stream(applicant.getSolicitor().getAddress().split("\n"))
                    .filter(value -> !Objects.equals(value, ""))
                    .toArray(String[]::new));
            templateContent.put(APPLICANT_2_SOLICITOR_ADDRESS, addressCleanUp);
        }
    }
}
