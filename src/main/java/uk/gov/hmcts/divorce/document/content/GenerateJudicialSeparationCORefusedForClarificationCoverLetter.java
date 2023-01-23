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

    public DocumentType getCoverLetterDocumentType(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
    }

    public String getCoverLetterDocumentTemplateId(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);
    }
}
