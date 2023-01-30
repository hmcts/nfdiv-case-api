package uk.gov.hmcts.divorce.document.content;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
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
