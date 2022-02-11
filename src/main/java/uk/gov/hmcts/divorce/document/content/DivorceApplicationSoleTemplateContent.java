package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant2Represented;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_KNOWS_RESPONDENTS_SOLICITOR_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE_POPULATED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class DivorceApplicationSoleTemplateContent {

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
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce from");
            templateContent.put(DIVORCE_OR_DISSOLUTION, "divorce application");
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce");
        } else {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH);
            templateContent.put(DIVORCE_OR_DISSOLUTION, "application to end your civil partnership");
            templateContent.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership");
        }

        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        if (application.getIssueDate() != null) {
            templateContent.put(ISSUE_DATE, application.getIssueDate().format(DATE_TIME_FORMATTER));
        }
        templateContent.put(ISSUE_DATE_POPULATED, application.getIssueDate() != null);

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_MIDDLE_NAME, applicant1.getMiddleName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
        templateContent.put(APPLICANT_1_FULL_NAME, application.getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_1_POSTAL_ADDRESS, applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant1));
        if (!applicant1.isConfidentialContactDetails()) {
            templateContent.put(APPLICANT_1_EMAIL, applicant1.getEmail());
        }
        if (null != applicant1.getFinancialOrder()) {
            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_1, applicant1.getFinancialOrder().toBoolean());
            templateContent.put(APPLICANT_1_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant1));
        }
        if (null != applicant1.getLegalProceedings()) {
            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_1, applicant1.getLegalProceedings().toBoolean());
            templateContent.put(APPLICANT_1_COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());
        }
        if (null != application.getApplicant1IsApplicant2Represented()) {
            boolean applicant1IsApplicant2Represented = application.getApplicant1IsApplicant2Represented() == Applicant2Represented.YES;
            templateContent.put(APPLICANT_1_KNOWS_RESPONDENTS_SOLICITOR_DETAILS, applicant1IsApplicant2Represented);
            if (applicant1IsApplicant2Represented) {
                setSolicitorDetails(templateContent, applicant2);
            }
        }

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_MIDDLE_NAME, applicant2.getMiddleName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
        templateContent.put(APPLICANT_2_FULL_NAME, application.getMarriageDetails().getApplicant2Name());
        templateContent.put(
            APPLICANT_2_POSTAL_ADDRESS,
            applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant2, application));
        if (!applicant2.isConfidentialContactDetails()) {
            templateContent.put(APPLICANT_2_EMAIL, applicant2.getEmail());
        }

        templateContent.put(PLACE_OF_MARRIAGE, application.getMarriageDetails().getPlaceOfMarriage());
        templateContent.put(MARRIAGE_DATE,
            ofNullable(application.getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));

        templateContent.put("jurisdictions", applicationTemplateDataProvider.deriveSoleJurisdictionList(application, caseId));

        return templateContent;
    }

    private void setSolicitorDetails(Map<String, Object> templateContent, Applicant applicant) {
        String solicitorName = applicant.getSolicitor().getName();
        String solicitorEmail = applicant.getSolicitor().getEmail();
        String solicitorFirmName = applicant.getSolicitor().getFirmName();
        String solicitorAddress = applicant.getSolicitor().getAddress();
        boolean hasEnteredSolicitorDetails =
            nonNull(solicitorName) || nonNull(solicitorEmail) || nonNull(solicitorFirmName) || nonNull(solicitorAddress);
        templateContent.put(APPLICANT_1_HAS_ENTERED_RESPONDENTS_SOLICITOR_DETAILS, hasEnteredSolicitorDetails);
        templateContent.put(APPLICANT_2_SOLICITOR_NAME, solicitorName);
        templateContent.put(APPLICANT_2_SOLICITOR_EMAIL, solicitorEmail);
        templateContent.put(APPLICANT_2_SOLICITOR_FIRM_NAME, solicitorFirmName);
        templateContent.put(APPLICANT_2_SOLICITOR_ADDRESS, solicitorAddress);
    }
}
