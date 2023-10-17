package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DocumentType;

@Component
@Slf4j
public class JudicialSeparationCORefusedForClarificationCoverLetter extends
    JudicialSeparationCoRefusalTemplateContent {

    @Autowired
    private ConditionalOrderCommonContent conditionalOrderCommonContent;


    @Override
    public DocumentType getCoverLetterDocumentType(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getCoverLetterDocumentType(caseData, applicant, true);
    }

    @Override
    public String getCoverLetterDocumentTemplateId(final CaseData caseData, final Applicant applicant) {
        return conditionalOrderCommonContent.getCoverLetterDocumentTemplateId(caseData, applicant, true);
    }
}
