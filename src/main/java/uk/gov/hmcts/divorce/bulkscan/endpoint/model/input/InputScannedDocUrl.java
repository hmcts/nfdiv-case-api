package uk.gov.hmcts.divorce.bulkscan.endpoint.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class InputScannedDocUrl {

    @JsonProperty("document_url")
    private String url;

    @JsonProperty("document_binary_url")
    private String binaryUrl;

    @JsonProperty("document_filename")
    private String filename;

}
