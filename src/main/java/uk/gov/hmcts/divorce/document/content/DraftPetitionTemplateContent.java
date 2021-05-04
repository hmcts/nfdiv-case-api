package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.FinancialOrderFor;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONDITIONAL_ORDER_OF_DIVORCE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_CASE_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_COSTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION_COST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FINANCIAL_ORDER_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FOR_A_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_COST_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS_FOR_CHILD;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.OF_THE_DIVORCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_A_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TO_END_THE_CIVIL_PARTNERSHIP;

@Component
@Slf4j
public class DraftPetitionTemplateContent {

    public Map<String, Object> apply(CaseData caseData, Long ccdCaseReference) {
        Map<String, Object> templateData = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateData.put(DIVORCE_OR_DISSOLUTION, FOR_A_DIVORCE);
            templateData.put(MARRIAGE_OR_RELATIONSHIP, MARRIAGE);
            templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, OF_THE_DIVORCE);
            templateData.put(FINANCIAL_ORDER_OR_DISSOLUTION, CONDITIONAL_ORDER_OF_DIVORCE_FROM);
            templateData.put(DIVORCE_OR_DISSOLUTION_COST, DIVORCE_COSTS);

        } else {
            templateData.put(DIVORCE_OR_DISSOLUTION, TO_END_A_CIVIL_PARTNERSHIP);
            templateData.put(MARRIAGE_OR_RELATIONSHIP, RELATIONSHIP);
            templateData.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateData.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_THE_CIVIL_PARTNERSHIP);
            templateData.put(FINANCIAL_ORDER_OR_DISSOLUTION, DISSOLUTION_OF_THE_CIVIL_PARTNERSHIP_WITH);
            templateData.put(DIVORCE_OR_DISSOLUTION_COST, COSTS_RELATED_TO_ENDING_THE_CIVIL_PARTNERSHIP);
        }


        templateData.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateData.put(ISSUE_DATE, caseData.getCreatedDate().toString());

        templateData.put(APPLICANT_1_FIRST_NAME, caseData.getPetitionerFirstName());
        templateData.put(APPLICANT_1_MIDDLE_NAME, caseData.getPetitionerMiddleName());
        templateData.put(APPLICANT_1_LAST_NAME, caseData.getPetitionerLastName());

        templateData.put(APPLICANT_2_FIRST_NAME, caseData.getRespondentFirstName());
        templateData.put(APPLICANT_2_MIDDLE_NAME, caseData.getRespondentMiddleName());
        templateData.put(APPLICANT_2_LAST_NAME, caseData.getRespondentLastName());

        templateData.put(APPLICANT_1_FULL_NAME, caseData.getMarriagePetitionerName());
        templateData.put(APPLICANT_2_FULL_NAME, caseData.getMarriageRespondentName());

        templateData.put(MARRIAGE_DATE, caseData.getMarriageDate());
        templateData.put(COURT_CASE_DETAILS, caseData.getLegalProceedingsDetails());

        templateData.put(HAS_COST_ORDERS, caseData.getDivorceCostsClaim());

        boolean hasFinancialOrders = YES.equals(caseData.getFinancialOrder());
        templateData.put(HAS_FINANCIAL_ORDERS, hasFinancialOrders);

        boolean hasFinancialOrdersForChild =
            null != caseData.getFinancialOrderFor()
                && caseData.getFinancialOrderFor().contains(FinancialOrderFor.CHILDREN);

        templateData.put(HAS_FINANCIAL_ORDERS_FOR_CHILD, hasFinancialOrdersForChild);
        templateData.put(FINANCIAL_ORDER_CHILD, CHILDREN_OF_THE_APPLICANT_AND_THE_RESPONDENT);

        String respondentPostalAddress;
        if (caseData.getRespondentHomeAddress() == null) {
            respondentPostalAddress = caseData.getDerivedRespondentSolicitorAddr();
        } else {
            StringJoiner formattedAddress = new StringJoiner("\n");
            formattedAddress
                .add(caseData.getRespondentHomeAddress().getAddressLine1())
                .add(caseData.getRespondentHomeAddress().getAddressLine2())
                .add(caseData.getRespondentHomeAddress().getAddressLine3())
                .add(caseData.getRespondentHomeAddress().getPostTown())
                .add(caseData.getRespondentHomeAddress().getCounty())
                .add(caseData.getRespondentHomeAddress().getPostCode())
                .add(caseData.getRespondentHomeAddress().getCountry());

            respondentPostalAddress = formattedAddress.toString();
        }
        templateData.put(RESPONDENT_POSTAL_ADDRESS, respondentPostalAddress);

        return templateData;
    }
}
