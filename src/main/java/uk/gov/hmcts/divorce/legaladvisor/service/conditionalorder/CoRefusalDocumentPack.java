package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;


import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.LetterType;

import java.util.Set;

public interface CoRefusalDocumentPack {

    Set<DocumentType> getDocumentPack();

    String getErrorMessage();

    LetterType getLetterType();

    String getCoverLetterTemplateId();
}
