package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.AND_FOR_THE_CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_1;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDER_APPLICANT_2;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_OTHER_COURT_CASES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JOINT_OR_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.OF_THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_THE_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class DivorceApplicationSoleTemplateContent {

    private static final DateTimeFormatter TEMPLATE_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");

    public Supplier<Map<String, Object>> apply(final CaseData caseData,
                                               final Long ccdCaseReference,
                                               final LocalDate createdDate) {

        return () -> {
            Map<String, Object> templateData = new HashMap<>();

            log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

            if (caseData.getApplicationType().isSole()) {
                templateData.put(JOINT_OR_SOLE, SOLE_APPLICATION);

                if (caseData.getDivorceOrDissolution().isDivorce()) {
                    templateData.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, CONDITIONAL_ORDER_OF_DIVORCE_FROM);
                } else {
                    templateData.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH);
                }
            } else {
                templateData.put(JOINT_OR_SOLE, JOINT_APPLICATION);

                if (caseData.getDivorceOrDissolution().isDivorce()) {
                    templateData.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, CONDITIONAL_ORDER_OF_DIVORCE);
                } else {
                    templateData.put(CONDITIONAL_ORDER_DIVORCE_OR_CIVIL_PARTNERSHIP, DISSOLUTION_OF_A_CIVIL_PARTNERSHIP);
                }
            }

            if (caseData.getDivorceOrDissolution().isDivorce()) {
                templateData.put(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE);
                templateData.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
                templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
                templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE);

            } else {
                templateData.put(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP);
                templateData.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
                templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
                templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_THE_CIVIL_PARTNERSHIP);
            }

            templateData.put(IS_SOLE, caseData.getApplicationType().isSole());
            templateData.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
            templateData.put(APPLICANT_1_MIDDLE_NAME, caseData.getApplicant1().getMiddleName());
            templateData.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());

            templateData.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
            templateData.put(APPLICANT_2_MIDDLE_NAME, caseData.getApplicant2().getMiddleName());
            templateData.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());

            templateData.put(HAS_FINANCIAL_ORDERS, caseData.getApplicant1().getFinancialOrder().toBoolean());
            templateData.put(HAS_FINANCIAL_ORDER_APPLICANT_1, caseData.getApplicant1().getFinancialOrder().toBoolean());

            if (null != caseData.getApplicant2().getFinancialOrder()) {
                templateData.put(HAS_FINANCIAL_ORDER_APPLICANT_2, caseData.getApplicant2().getFinancialOrder().toBoolean());
            }

            templateData.put(CCD_CASE_REFERENCE, ccdCaseReference);
            templateData.put(ISSUE_DATE, createdDate.format(TEMPLATE_DATE_FORMAT));
            templateData.put(MARRIAGE_DATE,
                ofNullable(caseData.getApplication().getMarriageDetails().getDate())
                    .map(marriageDate -> marriageDate.format(TEMPLATE_DATE_FORMAT))
                    .orElse(null));

            templateData.put(COURT_CASE_DETAILS, caseData.getApplicant1().getLegalProceedingsDetails());
            templateData.put(HAS_OTHER_COURT_CASES, caseData.getApplicant1().getLegalProceedings().toBoolean());
            templateData.put(FINANCIAL_ORDER_CHILD_SOLE, AND_FOR_THE_CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT);
            templateData.put(FINANCIAL_ORDER_CHILD_JOINT, CHILDREN_OF_THE_APPLICANT_1_AND_APPLICANT_2);

            String applicant1PostalAddress = deriveApplicant1PostalAddress(caseData.getApplicant1());
            String applicant2PostalAddress = deriveApplicant2PostalAddress(
                caseData.getApplicant2(), caseData.getApplication().isSolicitorApplication());

            templateData.put(APPLICANT_1_POSTAL_ADDRESS, applicant1PostalAddress);
            templateData.put(APPLICANT_2_POSTAL_ADDRESS, applicant2PostalAddress);

            return templateData;
        };
    }

    private String deriveApplicant1PostalAddress(Applicant applicant) {
        String applicantPostalAddress;
        AddressGlobalUK applicantHomeAddress = applicant.getHomeAddress();

        if (applicantHomeAddress == null) {
            applicantPostalAddress = applicant.getSolicitor().getAddress();
        } else {
            applicantPostalAddress =
                Stream.of(
                    applicantHomeAddress.getAddressLine1(),
                    applicantHomeAddress.getAddressLine2(),
                    applicantHomeAddress.getAddressLine3(),
                    applicantHomeAddress.getPostTown(),
                    applicantHomeAddress.getCounty(),
                    applicantHomeAddress.getPostCode(),
                    applicantHomeAddress.getCountry()
                )
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(joining("\n"));
        }
        return applicantPostalAddress;
    }

    private String deriveApplicant2PostalAddress(Applicant applicant, Boolean isSolicitorApplication) {
        String applicantPostalAddress;

        if (applicant.isRepresented()) {
            applicantPostalAddress = applicant.getSolicitor().getAddress();
        } else {
            AddressGlobalUK applicantHomeAddress =
                isSolicitorApplication ? applicant.getCorrespondenceAddress() : applicant.getHomeAddress();

            applicantPostalAddress =
                Stream.of(
                    applicantHomeAddress.getAddressLine1(),
                    applicantHomeAddress.getAddressLine2(),
                    applicantHomeAddress.getAddressLine3(),
                    applicantHomeAddress.getPostTown(),
                    applicantHomeAddress.getCounty(),
                    applicantHomeAddress.getPostCode(),
                    applicantHomeAddress.getCountry()
                )
                    .filter(value -> value != null && !value.isEmpty())
                    .collect(joining("\n"));
        }
        return applicantPostalAddress;
    }
}
