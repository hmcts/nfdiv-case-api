package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;
import uk.gov.hmcts.divorce.document.content.provider.ApplicantTemplateDataProvider;
import uk.gov.hmcts.divorce.document.content.provider.ApplicationTemplateDataProvider;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS_FOR_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_GIVEN;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_IS_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_FIRM_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class DraftApplicationTemplateContent {

    @Autowired
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Autowired
    private ApplicationTemplateDataProvider applicationTemplateDataProvider;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final var application = caseData.getApplication();
        final var isSole = caseData.getApplicationType().isSole();
        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();
        final var applicant2Solicitor = caseData.getApplicant2().getSolicitor();
        final Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            getDivorceContent(templateContent, isSole);
        } else {
            getDissolutionContent(templateContent, isSole);
        }

        if (!isNull(applicant1.getFinancialOrder())) {
            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_1, applicant1.getFinancialOrder().toBoolean());
        }

        if (!isNull(applicant2.getFinancialOrder())) {
            templateContent.put(HAS_FINANCIAL_ORDER_APPLICANT_2, applicant2.getFinancialOrder().toBoolean());
        }

        boolean hasFinancialOrdersForChild =
            nonNull(applicant1.getFinancialOrderFor())
                && applicant1.getFinancialOrderFor().contains(FinancialOrderFor.CHILDREN);

        templateContent.put(HAS_FINANCIAL_ORDERS_FOR_CHILD, hasFinancialOrdersForChild);

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);

        templateContent.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
        templateContent.put(APPLICANT_1_MIDDLE_NAME, applicant1.getMiddleName());
        templateContent.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
        templateContent.put(APPLICANT_2_MIDDLE_NAME, applicant2.getMiddleName());
        templateContent.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());

        templateContent.put(APPLICANT_1_FULL_NAME, application.getMarriageDetails().getApplicant1Name());
        templateContent.put(APPLICANT_2_FULL_NAME, application.getMarriageDetails().getApplicant2Name());

        templateContent.put(PLACE_OF_MARRIAGE, application.getMarriageDetails().getPlaceOfMarriage());

        templateContent.put(MARRIAGE_DATE,
            ofNullable(application.getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));

        if (isSole) {
            templateContent.put("jurisdictions",
                applicationTemplateDataProvider.deriveSoleJurisdictionList(application, ccdCaseReference));
        } else {
            templateContent.put("jurisdictions",
                applicationTemplateDataProvider.deriveJointJurisdictionList(application, ccdCaseReference));
        }

        if (!isNull(applicant1.getLegalProceedings())) {
            templateContent.put(HAS_OTHER_COURT_CASES_APPLICANT_1, applicant1.getLegalProceedings().toBoolean());
            templateContent.put(APPLICANT_1_COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());
        }

        templateContent.put(APPLICANT_1_POSTAL_ADDRESS, applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant1));
        templateContent.put(APPLICANT_1_EMAIL, applicant1.getEmail());
        templateContent.put(APPLICANT_2_POSTAL_ADDRESS,
            applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant2, application));
        templateContent.put(APPLICANT_2_EMAIL, applicant2.getEmail());

        if (isSole) {
            templateContent.put(RESPONDENT_IS_REPRESENTED, applicant2.isRepresented());
            if (applicant2.isRepresented()) {
                templateContent.put(RESPONDENT_SOLICITOR_NAME,
                    isNull(applicant2Solicitor.getName()) ? NOT_GIVEN : applicant2Solicitor.getName());
                templateContent.put(RESPONDENT_SOLICITOR_EMAIL,
                    isNull(applicant2Solicitor.getEmail()) ? NOT_GIVEN : applicant2Solicitor.getEmail());
                templateContent.put(RESPONDENT_SOLICITOR_FIRM_NAME,
                    isNull(applicant2Solicitor.getOrganisationPolicy())
                        ? NOT_GIVEN
                        : applicant2Solicitor.getOrganisationPolicy().getOrganisation().getOrganisationName());
                templateContent.put(RESPONDENT_SOLICITOR_ADDRESS,
                    isNull(applicant2Solicitor.getAddress()) ? NOT_GIVEN : applicant2Solicitor.getAddress());
            }
        }

        return templateContent;
    }

    private void getDivorceContent(Map<String, Object> templateContent, Boolean isSole) {
        templateContent.put(DIVORCE_OR_DISSOLUTION, "divorce application");
        templateContent.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce");

        if (isSole) {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce from");
        } else {
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce");
        }
    }

    private void getDissolutionContent(Map<String, Object> templateContent, Boolean isSole) {
        templateContent.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership");

        if (isSole) {
            templateContent.put(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP);
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP,
                "for the dissolution of the civil partnership with");
        } else {
            templateContent.put(DIVORCE_OR_DISSOLUTION, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);
            templateContent.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for the dissolution of their civil partnership");
        }
    }
}
