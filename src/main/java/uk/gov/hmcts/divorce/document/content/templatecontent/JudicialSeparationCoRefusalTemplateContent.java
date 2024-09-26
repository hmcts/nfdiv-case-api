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

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_FIRM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@RequiredArgsConstructor
@Slf4j
public class JudicialSeparationCoRefusalTemplateContent implements TemplateContent {

    private static final String IS_JOINT = "isJoint";

    private final Clock clock;
    private final DocmosisCommonContent docmosisCommonContent;
    private final ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            JUDICIAL_SEPARATION_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID,
            JUDICIAL_SEPARATION_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        return templateContent(caseData, caseId, applicant);
    }

    public Map<String, Object> templateContent(final CaseData caseData, final Long ccdCaseReference, final Applicant applicant) {

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(FEEDBACK, conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()));

        if (applicant.isRepresented()) {
            templateContent.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
            templateContent.put(SOLICITOR_FIRM, applicant.getSolicitor().getFirmName());
            templateContent.put(SOLICITOR_ADDRESS, applicant.getSolicitor().getAddress());
            templateContent.put(SOLICITOR_REFERENCE, getSolicitorReference(applicant));
            templateContent.put(APPLICANT_1_SOLICITOR_NAME, getSolicitorName(caseData.getApplicant1()));
            templateContent.put(APPLICANT_2_SOLICITOR_NAME, getSolicitorName(caseData.getApplicant2()));
            templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
            templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        } else {
            templateContent.put(FIRST_NAME, applicant.getFirstName());
            templateContent.put(LAST_NAME, applicant.getLastName());
            templateContent.put(ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());

            if (caseData.getDivorceOrDissolution().isDivorce()) {
                templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
                templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
            } else {
                templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
                templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            }

            templateContent.put(PARTNER, conditionalOrderCommonContent.getPartner(caseData));
        }

        return templateContent;
    }

    private String getSolicitorName(final Applicant applicant) {
        if (applicant.isRepresented()) {
            return applicant.getSolicitor().getName();
        }
        return WELSH.equals(applicant.getLanguagePreference())
            ? "nas cynrychiolwyd"
            : "not represented";
    }

    private String getSolicitorReference(final Applicant applicant) {
        if (applicant.isRepresented() && applicant.getSolicitor().getReference() != null) {
            return applicant.getSolicitor().getReference();
        }
        return WELSH.equals(applicant.getLanguagePreference())
            ? "heb ei ddarparu"
            : "not provided";
    }
}
