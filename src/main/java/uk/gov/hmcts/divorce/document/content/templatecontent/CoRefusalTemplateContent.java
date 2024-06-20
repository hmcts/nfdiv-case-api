package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderCommonContent;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForAmendmentContent.LEGAL_ADVISOR_COMMENTS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CP_CASE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoRefusalTemplateContent implements TemplateContent {

    private final ConditionalOrderCommonContent conditionalOrderCommonContent;
    private final Clock clock;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID, REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return this.templateContent(caseData, caseId, applicant);
    }

    public Map<String, Object> templateContent(final CaseData caseData,
                                                final Long caseId,
                                                final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(caseId));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(
            LEGAL_ADVISOR_COMMENTS,
            conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder())
        );

        templateContent.put(
            DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL,
            caseData.isDivorce() ? CONTACT_DIVORCE_EMAIL : CP_CASE_EMAIL
        );

        return templateContent;
    }
}
