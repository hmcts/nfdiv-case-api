package uk.gov.hmcts.reform.divorce.caseapi.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CcdCallbackRequest {
    @ApiModelProperty(value = "Authorisation token of the user which triggered callback event")
    private String token;

    @ApiModelProperty(value = "Event Id which triggered the callback")
    @JsonProperty("event_id")
    private String eventId;

    @ApiModelProperty(value = "Case data")
    @JsonProperty("case_details")
    private CaseDetails caseDetails;
}
