package uk.gov.hmcts.divorce.bulkscan.endpoint.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OcrValidationResponse {

    @JsonProperty("warnings")
    private List<String> warnings;

    @JsonProperty("errors")
    private List<String> errors;

    @JsonProperty("status")
    private ValidationStatus status;
}
