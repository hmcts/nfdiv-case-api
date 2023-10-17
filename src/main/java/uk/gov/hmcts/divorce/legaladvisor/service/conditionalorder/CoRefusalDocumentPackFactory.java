package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;

public class CoRefusalDocumentPackFactory {
    public static CoRefusalDocumentPack getDocumentPackToGenerate(CaseData caseData,
                                                                  Applicant applicant) {
        if (caseData.isJudicialSeparationCase()) {
            if (MORE_INFO.equals(caseData.getConditionalOrder().getRefusalDecision())) {
                if (applicant.isRepresented()) {
                    return caseData.isDivorce() ? new ClarificationRefusalJsSolicitorPack() : new ClarificationRefusalSolSeparationPack();
                }
                return caseData.isDivorce() ? new ClarificationRefusalJsPack() : new ClarificationRefusalSeparationPack();
            } else {
                if (applicant.isRepresented()) {
                    return caseData.isDivorce() ? new AmendmentRefusalJsSolicitorPack() : new AmendmentRefusalSolSeparationPack();
                }
                return caseData.isDivorce() ? new AmendmentRefusalJsPack() : new AmendmentRefusalSeparationPack();
            }
        }

        return MORE_INFO.equals(caseData.getConditionalOrder().getRefusalDecision())
            ? new ClarificationRefusalPack() : new AmendmentRefusalPack();
    }
}
