package uk.gov.hmcts.divorce.bulkscan.endpoint.model.output;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.CaseCreationDetails;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class SuccessfulTransformationResponse {

    @JsonProperty("case_creation_details")
    private final CaseCreationDetails caseCreationDetails;

    @JsonProperty("warnings")
    @Builder.Default
    private final List<String> warnings = new ArrayList<>();
}
