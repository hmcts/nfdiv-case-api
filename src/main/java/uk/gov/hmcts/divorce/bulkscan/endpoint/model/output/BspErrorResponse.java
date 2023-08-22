package uk.gov.hmcts.divorce.bulkscan.endpoint.model.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class BspErrorResponse {

    @JsonProperty("errors")
    @Builder.Default
    private final List<String> errors = new ArrayList<>();

    @JsonProperty("warnings")
    @Builder.Default
    private final List<String> warnings = new ArrayList<>();
}
