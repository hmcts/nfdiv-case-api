package uk.gov.hmcts.divorce.document.content;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SPOUSE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SPOUSE_WELSH;

@Component
public class ConditionalOrderCommonContent {

    @Autowired
    private CommonContent commonContent;

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

    public DocumentType getCoverLetterDocumentType(
        final CaseData caseData,
        final Applicant applicant,
        final boolean isClarificationRefusal
    ) {
        if (caseData.isJudicialSeparationCase()) {
            if (isClarificationRefusal) {
                return applicant.isRepresented()
                    ? JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER
                    : JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER;
            }
            return applicant.isRepresented()
                ? JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER
                : JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
        }
        return CONDITIONAL_ORDER_REFUSAL_COVER_LETTER;
    }

    public String getCoverLetterDocumentTemplateId(
        final CaseData caseData,
        final Applicant applicant,
        final boolean isClarificationRefusal
    ) {
        if (caseData.isJudicialSeparationCase()) {
            if (isClarificationRefusal) {
                return applicant.isRepresented()
                    ? JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
                    : JUDICIAL_SEPARATION_CONDITIONAL_ORDER_CLARIFICATION_REFUSAL_COVER_LETTER_TEMPLATE_ID;
            }
            return applicant.isRepresented()
                ? JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_SOLICITOR_COVER_LETTER_TEMPLATE_ID
                : JUDICIAL_SEPARATION_CONDITIONAL_ORDER_REFUSAL_COVER_LETTER_TEMPLATE_ID;
        }
        return isClarificationRefusal
            ? CLARIFICATION_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID
            : REJECTED_REFUSAL_ORDER_COVER_LETTER_TEMPLATE_ID;
    }

    public String getPartner(final CaseData caseData) {
        String partner = commonContent.getPartner(caseData, caseData.getApplicant2(),
            caseData.getApplicant1().getLanguagePreference());

        if (caseData.isDivorce() && caseData.getApplicant1().isApplicantOffline()) {
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
