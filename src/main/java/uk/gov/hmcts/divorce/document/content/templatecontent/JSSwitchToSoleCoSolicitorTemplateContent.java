package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_REPRESENTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class JSSwitchToSoleCoSolicitorTemplateContent implements TemplateContent {

    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(JUDICIAL_SEPARATION_SWITCH_TO_SOLE_SOLICITOR_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {
        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            caseData.getApplicant1().getLanguagePreference());

        templateContent.put(RESPONDENT_SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        templateContent.put(RESPONDENT_SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
            caseData.getDivorceOrDissolution().isDivorce() ? MARRIAGE : CIVIL_PARTNERSHIP);
        templateContent.put(SOLICITOR_REFERENCE, isNotEmpty(caseData.getApplicant2().getSolicitor().getReference())
            ? caseData.getApplicant2().getSolicitor().getReference()
            : NOT_PROVIDED);

        templateContent.put(APPLICANT_1_SOLICITOR_NAME, caseData.getApplicant1().isRepresented()
            ? caseData.getApplicant1().getSolicitor().getName()
            : NOT_REPRESENTED);

        templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant1(),
            caseData.getApplicant2().getLanguagePreference()));

        return templateContent;
    }
}
