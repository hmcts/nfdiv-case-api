package uk.gov.hmcts.divorce.model.docassembly;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DocAssemblyResponse {
    private String renditionOutputLocation;

    public String getBinaryFilePath() {
        return renditionOutputLocation + "/binary";
    }
}
