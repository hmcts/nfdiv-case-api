package uk.gov.hmcts.divorce.document.print.documentpack;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.DocumentGenerationUtil;
import uk.gov.hmcts.divorce.document.model.DocumentType;

public enum DocumentGenerationPack {
    CERTIFICATE_OF_ENTITLEMENT(DocumentGenerationUtil::generateDocuments);
    public final DocumentPackGenerator<DocumentGenerationUtil, CaseDetails<CaseData, State>, DocumentPack, List<DocumentType>> generateDocuments;

    DocumentGenerationPack(DocumentPackGenerator<DocumentGenerationUtil, CaseDetails<CaseData, State>, DocumentPack, List<DocumentType>> generateDocuments) {
        this.generateDocuments = generateDocuments;
    }
}

