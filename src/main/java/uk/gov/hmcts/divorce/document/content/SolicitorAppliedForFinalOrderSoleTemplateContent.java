package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_APP1_SOLICITOR_APPLIED_FOR_FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_OVERDUE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolicitorAppliedForFinalOrderSoleTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;
    private final Clock clock;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
                NFD_APP1_SOLICITOR_APPLIED_FOR_FINAL_ORDER_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
                .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", caseId, caseData.getDivorceOrDissolution());

        templateContent.put(ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(APPLICANT_1_FULL_NAME,  applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME,  caseData.getApplicant2().getFullName());
        templateContent.put(SOLICITOR_NAME,  applicant.getSolicitor().getName());
        templateContent.put(IS_OVERDUE,  YesOrNo.YES == caseData.getFinalOrder().getIsFinalOrderOverdue());
        templateContent.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP,  caseData.isDivorce() ? "marriage" : "civil partnership");
        templateContent.put(SOLICITOR_ADDRESS,  applicant.getSolicitor().getAddress());
        templateContent.put(SOLICITOR_REFERENCE,  isNotEmpty(applicant.getSolicitor().getReference())
                ? applicant.getSolicitor().getReference() : NOT_PROVIDED);

        return templateContent;
    }
}
