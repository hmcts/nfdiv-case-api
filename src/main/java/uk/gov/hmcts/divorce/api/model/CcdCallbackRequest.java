package uk.gov.hmcts.divorce.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
// TODO remove in favour of CCD generator lib
public class CcdCallbackRequest {
    private String token;
    @JsonProperty("event_id")
    private String eventId;
    @JsonProperty("case_details")
    private CaseDetails caseDetails;
}
