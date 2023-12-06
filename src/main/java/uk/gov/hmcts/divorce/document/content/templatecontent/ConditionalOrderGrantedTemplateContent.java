package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COUNTRY_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CO_PRONOUNCED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DOCUMENTS_ISSUED_ON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_SOLE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.JUDGE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PLACE_OF_MARRIAGE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;


@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionalOrderGrantedTemplateContent implements TemplateContent {

    private CommonContent commonContent;

    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CONDITIONAL_ORDER_PRONOUNCED_TEMPLATE_ID, JUDICIAL_SEPARATION_ORDER_PRONOUNCED_TEMPLATE_ID);
    }

    @Value("${final_order.eligible_from_offset_days}")
    private long finalOrderOffsetDays;

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return apply(caseData, caseId);
    }

    private Map<String, Object> apply(final CaseData caseData, final Long caseId) {


        log.info("For ccd case reference {} ", caseId);

        final var applicant1 = caseData.getApplicant1();
        final var applicant2 = caseData.getApplicant2();
        final boolean isDivorce = caseData.getDivorceOrDissolution().isDivorce();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference());

        templateContent.put(JUDGE_NAME, conditionalOrder.getPronouncementJudge());
        templateContent.put(COURT_NAME, conditionalOrder.getCourt() != null ? conditionalOrder.getCourt().getLabel() : null);
        templateContent.put(CO_PRONOUNCED_DATE, conditionalOrder.getGrantedDate() != null
            ? conditionalOrder.getGrantedDate().format(DATE_TIME_FORMATTER) : null);
        templateContent.put(DATE_FO_ELIGIBLE_FROM, conditionalOrder.getGrantedDate() != null
            ? conditionalOrder.getGrantedDate().plusDays(finalOrderOffsetDays).format(DATE_TIME_FORMATTER) : null);

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_DIVORCE, isDivorce);
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(DOCUMENTS_ISSUED_ON, conditionalOrder.getDateAndTimeOfHearing() != null
            ? conditionalOrder.getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER) : null);
        templateContent.put(APPLICANT_1_FULL_NAME, applicant1.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, applicant2.getFullName());
        templateContent.put(PARTNER, commonContent.getPartner(caseData, caseData.getApplicant2(), caseData.getApplicant2().getLanguagePreference()));
        templateContent.put(PLACE_OF_MARRIAGE, caseData.getApplication().getMarriageDetails().getPlaceOfMarriage());
        templateContent.put(COUNTRY_OF_MARRIAGE, caseData.getApplication().getMarriageDetails().getCountryOfMarriage());
        templateContent.put(MARRIAGE_DATE,
            ofNullable(caseData.getApplication().getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));

        if (WELSH.equals(caseData.getApplicant1().getLanguagePreference()) || (WELSH.equals(caseData.getApplicant2().getLanguagePreference()))) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isDivorce ? MARRIAGE_CY : CIVIL_PARTNERSHIP_CY);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, isDivorce ? MARRIAGE : CIVIL_PARTNERSHIP);
        }

        return templateContent;
    }
}
