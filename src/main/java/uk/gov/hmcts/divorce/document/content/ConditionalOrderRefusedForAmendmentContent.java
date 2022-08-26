package uk.gov.hmcts.divorce.document.content;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SPOUSE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SPOUSE_WELSH;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class ConditionalOrderRefusedForAmendmentContent {

    public static final String LEGAL_ADVISOR_COMMENTS = "legalAdvisorComments";
    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";

    @Autowired
    private Clock clock;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        LanguagePreference languagePreference = caseData.getApplicant1().getLanguagePreference();

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

        templateContent.put(LEGAL_ADVISOR_COMMENTS, generateLegalAdvisorComments(conditionalOrder));

        templateContent.put(PARTNER, getPartner(caseData));

        return templateContent;
    }

    public List<RefusalReason> generateLegalAdvisorComments(ConditionalOrder conditionalOrder) {

        if (MORE_INFO.equals(conditionalOrder.getRefusalDecision())) {

            Set<ClarificationReason> refusalClarificationReason = conditionalOrder.getRefusalClarificationReason();

            if (isEmpty(refusalClarificationReason)) {
                return emptyList();
            }

            List<RefusalReason> legalAdvisorComments = refusalClarificationReason.stream()
                .filter(clarificationReason -> !clarificationReason.equals(ClarificationReason.OTHER))
                .map(reason -> new RefusalReason(reason.getLabel()))
                .collect(Collectors.toList());

            String refusalClarificationAdditionalInfo = conditionalOrder.getRefusalClarificationAdditionalInfo();
            if (isNotEmpty(refusalClarificationAdditionalInfo)) {
                legalAdvisorComments.add(new RefusalReason(refusalClarificationAdditionalInfo));
            }

            return legalAdvisorComments;

        } else {
            List<RefusalReason> legalAdvisorComments = new ArrayList<>();

            String refusalRejectionAdditionalInfo = conditionalOrder.getRefusalRejectionAdditionalInfo();
            if (isNotEmpty(refusalRejectionAdditionalInfo)) {
                legalAdvisorComments.add(new RefusalReason(refusalRejectionAdditionalInfo));
            }

            return legalAdvisorComments;

        }
    }

    private String getPartner(final CaseData caseData) {
        String partner = commonContent.getPartner(caseData, caseData.getApplicant2(),
            caseData.getApplicant1().getLanguagePreference());

        if (caseData.isDivorce() && caseData.getApplicant1().isOffline()) {
            partner = WELSH.equals(caseData.getApplicant1().getLanguagePreference()) ? SPOUSE_WELSH : SPOUSE;
        }

        return partner;
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class RefusalReason {
        private String comment;
    }
}
