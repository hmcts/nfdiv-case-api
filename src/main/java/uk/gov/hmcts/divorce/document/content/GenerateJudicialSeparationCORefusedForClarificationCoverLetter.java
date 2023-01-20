package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

@Component
@Slf4j
public class GenerateJudicialSeparationCORefusedForClarificationCoverLetter extends
    GenerateJudicialSeparationCORefusedForAmendmentCoverLetter {

    @Autowired
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    public DocumentType getDocumentType(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getDocumentType(caseData, applicant, true);
    }

    public String getDocumentTemplateId(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getDocumentTemplateId(caseData, applicant, true);
    }
}
