package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.*;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class JudicialSeparationApplicationJointTemplateContent {

    @Autowired
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Autowired
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    public Map<String, Object> apply(final CaseData caseData, final Long caseId) {

        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        final Application application = caseData.getApplication();
        final Applicant applicant1 = caseData.getApplicant1();
        final Applicant applicant2 = caseData.getApplicant2();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a judicial separation order.");
            templateContent.put(DIVORCE_OR_DISSOLUTION, "judicial separation application");
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the judicial separation");
        } else {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a separation order.");
            templateContent.put(DIVORCE_OR_DISSOLUTION, "application to end a civil partnership");
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "separation");
        }

        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        if (application.getIssueDate() != null) {
            templateContent.put(ISSUE_DATE, application.getIssueDate().format(DATE_TIME_FORMATTER));
        }
        templateContent.put(ISSUE_DATE_POPULATED, application.getIssueDate() != null);

        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_1_MARRIAGE_NAME, application.getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_1_POSTAL_ADDRESS, applicant1.getCorrespondenceAddress());
        if (!applicant1.isConfidentialContactDetails()) {
            templateContent.put(APPLICANT_1_EMAIL, applicant1.getEmail());
        }
        if (null != applicant1.getFinancialOrder()) {
            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_1, applicant1.getFinancialOrder().toBoolean());
            templateContent.put(APPLICANT_1_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveJointFinancialOrder(applicant1, false));
        }
        if (null != applicant1.getLegalProceedings()) {
            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_1, applicant1.getLegalProceedings().toBoolean());
            templateContent.put(APPLICANT_1_COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());
        }

        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(APPLICANT_2_MARRIAGE_NAME, application.getMarriageDetails().getApplicant2Name());

        applicantTemplateDataProvider.mapContactDetails(applicant1, applicant2, templateContent);

        if (null != applicant2.getFinancialOrder()) {
            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_2, applicant2.getFinancialOrder().toBoolean());
            templateContent.put(APPLICANT_2_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveJointFinancialOrder(applicant2, false));
        }
        if (null != applicant2.getLegalProceedings()) {
            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_2, applicant2.getLegalProceedings().toBoolean());
            templateContent.put(APPLICANT_2_COURT_CASE_DETAILS, applicant2.getLegalProceedingsDetails());
        }

        applicationTemplateDataProvider.mapMarriageDetails(templateContent, application);

        templateContent.put(JURISDICTIONS, applicationTemplateDataProvider.deriveJurisdictionList(
            application, caseId, ENGLISH));

        return templateContent;
    }
}
