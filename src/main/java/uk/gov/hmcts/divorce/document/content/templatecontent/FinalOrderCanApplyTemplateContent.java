package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_CAN_APPLY_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.FINAL_ORDER_OVERDUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinalOrderCanApplyTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;
    private final CommonContent commonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(FINAL_ORDER_CAN_APPLY_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(IS_DIVORCE, caseData.isDivorce());
        templateContent.put(DATE, LocalDate.now().format(DATE_TIME_FORMATTER));
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
        templateContent.put(FINAL_ORDER_OVERDUE_DATE, caseData.getFinalOrder().getDateFinalOrderEligibleFrom().plusMonths(12)
                .format(getDateTimeFormatterForPreferredLanguage(applicant.getLanguagePreference())));
        templateContent.put(PARTNER, getPartnerInfo(caseData,applicant,applicant.getLanguagePreference()));

        return templateContent;
    }

    private String getPartnerInfo(CaseData caseData, Applicant applicant, LanguagePreference languagePreference) {
        if (applicant.equals(caseData.getApplicant1())) {
            return commonContent.getPartner(caseData, caseData.getApplicant2(), languagePreference);
        }
        return commonContent.getPartner(caseData, caseData.getApplicant1(), languagePreference);
    }
}
