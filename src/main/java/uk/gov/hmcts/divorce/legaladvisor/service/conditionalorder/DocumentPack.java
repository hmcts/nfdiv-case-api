package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public interface DocumentPack {
    DocumentPackInfo getDocumentPack(CaseData caseData, Applicant applicant);

    String getLetterId();
}
