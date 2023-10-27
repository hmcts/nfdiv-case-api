package uk.gov.hmcts.divorce.document.print.documentpack;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Map;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentPackInfo {
    private Map<DocumentType, Optional<String>> documentPack;
    private Map<String, String> templateInfo;
    public static DocumentPackInfo of(Map<DocumentType, Optional<String>> documentPack, Map<String, String> templateInfo) {
        return new DocumentPackInfo(documentPack, templateInfo);
    }
}
