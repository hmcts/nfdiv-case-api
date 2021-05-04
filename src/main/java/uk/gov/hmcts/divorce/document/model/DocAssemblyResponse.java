package uk.gov.hmcts.divorce.document.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocAssemblyResponse {
    private String renditionOutputLocation;

    public String getBinaryFilePath() {
        return renditionOutputLocation + "/binary";
    }
}
