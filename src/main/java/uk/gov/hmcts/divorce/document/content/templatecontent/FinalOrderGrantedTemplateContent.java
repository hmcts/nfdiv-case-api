package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinalOrderGrantedTemplateContent implements TemplateContent {

    public static final String THE_MARRIAGE_OR_CP = "theMarriageOrCp";
    public static final String SECTION = "section";
    public static final String SPOUSE_OR_CP = "spouseOrCp";
    public static final String THE_MARRIAGE = "the marriage";
    public static final String A_CIVIL_PARTNERSHIP = "a civil partnership";
    public static final String SECTION_18A = "18A";
    public static final String SECTION_18C = "18C";
    public static final String SPOUSE = "spouse";
    public static final String SPOUSE_CY = "cyn-briod";
    public static final String DIVORCE = "Divorce";
    public static final String DIVORCE_CY = "ysgariad";
    public static final String DISSOLUTION_OF_A_CIVIL_PARTNERSHIP = "Dissolution of a civil partnership";
    public static final String DISSOLUTION_OF_A_CIVIL_PARTNERSHIP_CY = "diddymu partneriaeth sifil";
    public static final String FORMER_CIVIL_PARTNER_CY = "cyn-bartner sifil";

    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(FINAL_ORDER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return apply(caseData, caseId);
    }

    private Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference());

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(DATE, isNotEmpty(caseData.getFinalOrder().getGrantedDate())
            ? caseData.getFinalOrder().getGrantedDate().format(DATE_TIME_FORMATTER) : EMPTY);
        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());

        ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        templateContent.put(CO_PRONOUNCED_DATE, isNotEmpty(conditionalOrder.getGrantedDate())
            ? conditionalOrder.getGrantedDate().format(DATE_TIME_FORMATTER) : EMPTY);

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        boolean isDivorce = caseData.isDivorce();

        if (WELSH.equals(caseData.getApplicant1().getLanguagePreference())) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isDivorce ? MARRIAGE_CY : CIVIL_PARTNERSHIP_CY);
            templateContent.put(THE_MARRIAGE_OR_CP, isDivorce ? MARRIAGE_CY : CIVIL_PARTNERSHIP_CY);
            templateContent.put(SPOUSE_OR_CP, isDivorce ? SPOUSE_CY : FORMER_CIVIL_PARTNER_CY);
            templateContent.put(DIVORCE_OR_DISSOLUTION, isDivorce ? DIVORCE_CY : DISSOLUTION_OF_A_CIVIL_PARTNERSHIP_CY);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isDivorce ? MARRIAGE : CIVIL_PARTNERSHIP);
            templateContent.put(THE_MARRIAGE_OR_CP, isDivorce ? THE_MARRIAGE : A_CIVIL_PARTNERSHIP);
            templateContent.put(SPOUSE_OR_CP, isDivorce ? SPOUSE : CIVIL_PARTNER);
            templateContent.put(DIVORCE_OR_DISSOLUTION, isDivorce ? DIVORCE : DISSOLUTION_OF_A_CIVIL_PARTNERSHIP);
        }

        templateContent.put(SECTION, isDivorce ? SECTION_18A : SECTION_18C);

        MarriageDetails marriageDetails = caseData.getApplication().getMarriageDetails();
        templateContent.put(PLACE_OF_MARRIAGE, marriageDetails.getPlaceOfMarriage());
        templateContent.put(COUNTRY_OF_MARRIAGE, marriageDetails.getCountryOfMarriage());
        templateContent.put(MARRIAGE_DATE,
            ofNullable(marriageDetails.getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(EMPTY));

        return templateContent;
    }
}
