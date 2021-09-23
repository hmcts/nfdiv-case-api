package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.part.ApplicantTemplateDataProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;

@Component
@Slf4j
public class ApplicationJointTemplateContent {

    private static final DateTimeFormatter TEMPLATE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    @Autowired
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    public Supplier<Map<String, Object>> apply(final CaseData caseData,
                                               final Long ccdCaseReference,
                                               final LocalDate createdDate) {

        return () -> {
            final Map<String, Object> templateData = new HashMap<>();

            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

            final Application application = caseData.getApplication();
            final Applicant applicant1 = caseData.getApplicant1();
            final Applicant applicant2 = caseData.getApplicant2();

            if (caseData.getDivorceOrDissolution().isDivorce()) {
                templateData.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for a final order of divorce.");
                templateData.put(DIVORCE_OR_DISSOLUTION, "divorce application");
                templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
                templateData.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
                templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "the divorce");
            } else {
                templateData.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, "for the dissolution of their civil partnership.");
                templateData.put(DIVORCE_OR_DISSOLUTION, "application to end a civil partnership");
                templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
                templateData.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
                templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, "ending the civil partnership");
            }

            templateData.put(CCD_CASE_REFERENCE, ccdCaseReference);
            templateData.put(ISSUE_DATE, createdDate.format(TEMPLATE_DATE_FORMAT));

            templateData.put(APPLICANT_1_FIRST_NAME, applicant1.getFirstName());
            templateData.put(APPLICANT_1_MIDDLE_NAME, applicant1.getMiddleName());
            templateData.put(APPLICANT_1_LAST_NAME, applicant1.getLastName());
            templateData.put(APPLICANT_1_FULL_NAME, application.getMarriageDetails().getApplicant1Name());
            templateData.put(APPLICANT_1_POSTAL_ADDRESS, applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant1));
            if (applicant1.hasShareableContactDetails()) {
                templateData.put(APPLICANT_1_EMAIL, applicant1.getEmail());
            }
            if (null != applicant1.getFinancialOrder()) {
                templateData.put(HAS_FINANCIAL_ORDER_APPLICANT_1, applicant1.getFinancialOrder().toBoolean());
                templateData.put(APPLICANT_1_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveFinancialOrder(applicant1));
            }
            if (null != applicant1.getLegalProceedings()) {
                templateData.put(HAS_OTHER_COURT_CASES_APPLICANT_1, applicant1.getLegalProceedings().toBoolean());
                templateData.put(APPLICANT_1_COURT_CASE_DETAILS, applicant1.getLegalProceedingsDetails());
            }

            templateData.put(APPLICANT_2_FIRST_NAME, applicant2.getFirstName());
            templateData.put(APPLICANT_2_MIDDLE_NAME, applicant2.getMiddleName());
            templateData.put(APPLICANT_2_LAST_NAME, applicant2.getLastName());
            templateData.put(APPLICANT_2_FULL_NAME, application.getMarriageDetails().getApplicant2Name());
            templateData.put(APPLICANT_2_POSTAL_ADDRESS, applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant2));
            if (applicant2.hasShareableContactDetails()) {
                templateData.put(APPLICANT_2_EMAIL, applicant2.getEmail());
            }
            if (null != applicant2.getFinancialOrder()) {
                templateData.put(HAS_FINANCIAL_ORDER_APPLICANT_2, applicant2.getFinancialOrder().toBoolean());
                templateData.put(APPLICANT_2_FINANCIAL_ORDER, applicantTemplateDataProvider.deriveFinancialOrder(applicant2));
            }
            if (null != applicant2.getLegalProceedings()) {
                templateData.put(HAS_OTHER_COURT_CASES_APPLICANT_2, applicant2.getLegalProceedings().toBoolean());
                templateData.put(APPLICANT_2_COURT_CASE_DETAILS, applicant2.getLegalProceedingsDetails());
            }

            templateData.put(PLACE_OF_MARRIAGE, application.getMarriageDetails().getPlaceOfMarriage());
            templateData.put(MARRIAGE_DATE,
                ofNullable(application.getMarriageDetails().getDate())
                    .map(marriageDate -> marriageDate.format(TEMPLATE_DATE_FORMAT))
                    .orElse(null));

            return templateData;
        };
    }
}
