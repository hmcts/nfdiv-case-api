package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_OFFLINE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class ConditionalOrderRefusedForClarificationContent implements ConditionalOrderRefusedTemplateContent {

    public static final String LEGAL_ADVISOR_COMMENTS = "legalAdvisorComments";
    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";
    public static final String JUDICIAL_SEPARATION = "judicialSeparation";
    public static final String REASON_JURISDICTION_DETAILS = "jurisdictionDetails";
    public static final String REASON_MARRIAGE_CERT_TRANSLATION = "marriageCertTranslation";
    public static final String REASON_MARRIAGE_CERTIFICATE = "marriageCertificate";
    public static final String REASON_PREVIOUS_PROCEEDINGS_DETAILS = "previousProceedingDetails";

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

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final Set<ClarificationReason> clarificationReasons = conditionalOrder.getRefusalClarificationReason();

        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(JUDICIAL_SEPARATION, caseData.isJudicialSeparationCase());

        templateContent.put(REASON_JURISDICTION_DETAILS,
            clarificationReasons.contains(ClarificationReason.JURISDICTION_DETAILS));
        templateContent.put(REASON_MARRIAGE_CERT_TRANSLATION,
            clarificationReasons.contains(ClarificationReason.MARRIAGE_CERTIFICATE_TRANSLATION));
        templateContent.put(REASON_MARRIAGE_CERTIFICATE,
            clarificationReasons.contains(ClarificationReason.MARRIAGE_CERTIFICATE));
        templateContent.put(REASON_PREVIOUS_PROCEEDINGS_DETAILS,
            clarificationReasons.contains(ClarificationReason.PREVIOUS_PROCEEDINGS_DETAILS));

        templateContent.put(LEGAL_ADVISOR_COMMENTS, conditionalOrderCommonContent.generateLegalAdvisorComments(conditionalOrder));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, WELSH.equals(languagePreference) ? MARRIAGE_CY : MARRIAGE);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, WELSH.equals(languagePreference) ? CIVIL_PARTNERSHIP_CY : CIVIL_PARTNERSHIP);
        }

        templateContent.put(IS_OFFLINE, caseData.getApplication().isPaperCase());

        return templateContent;
    }
}
