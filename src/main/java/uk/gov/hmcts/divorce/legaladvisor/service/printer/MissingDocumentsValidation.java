package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.Builder;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

@Builder(toBuilder = true)
public class MissingDocumentsValidation {
    @Builder.Default
    public String message = "";
    @Builder.Default
    public List<DocumentType> documentTypeList = List.of();
    @Builder.Default
    public int expectedDocumentsSize = 0;
}
