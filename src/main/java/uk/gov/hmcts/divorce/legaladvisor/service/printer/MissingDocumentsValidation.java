package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.Builder;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

@Builder(toBuilder = true)
public class MissingDocumentsValidation {
    public String message;
    public List<DocumentType> documentTypeList;
    public int expectedDocumentsSize;
}
