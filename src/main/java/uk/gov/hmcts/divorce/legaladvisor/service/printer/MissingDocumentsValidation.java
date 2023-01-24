package uk.gov.hmcts.divorce.legaladvisor.service.printer;

import lombok.Builder;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Builder(toBuilder = true)
public class MissingDocumentsValidation {
    @Builder.Default
    public String message = "Warning Message When Insufficient Documents Are Found";
    @Builder.Default
    public List<DocumentType> documentTypeList = List.of(COVERSHEET);
    @Builder.Default
    public int expectedDocumentsSize = 1;
}
