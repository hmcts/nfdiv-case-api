package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class ConditionalOrderRefusedForAmendmentContent implements ConditionalOrderRefusedTemplateContent {

    public static final String LEGAL_ADVISOR_COMMENTS = "legalAdvisorComments";
    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";

    @Autowired
    private Clock clock;

    @Autowired
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    @Override
    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        LanguagePreference languagePreference = caseData.getApplicant1().getLanguagePreference();

        Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(languagePreference);

        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? MARRIAGE_CY : MARRIAGE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? DIVORCE_WELSH : DIVORCE);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? CIVIL_PARTNERSHIP_CY : CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP,
                WELSH.equals(languagePreference) ? CIVIL_PARTNERSHIP_CY : CIVIL_PARTNERSHIP);
        }

        templateContent.put(IS_JUDICIAL_SEPARATION, caseData.isJudicialSeparationCase());

        templateContent.put(
            LEGAL_ADVISOR_COMMENTS, conditionalOrderCommonContent.generateLegalAdvisorComments(caseData.getConditionalOrder()));

        templateContent.put(PARTNER, conditionalOrderCommonContent.getPartner(caseData));

        return templateContent;
    }
}
