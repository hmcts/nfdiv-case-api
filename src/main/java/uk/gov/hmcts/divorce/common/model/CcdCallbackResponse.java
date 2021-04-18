package uk.gov.hmcts.divorce.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response to a callback from ccd")
@Builder
// TODO remove in favour of CCD generator lib
public class CcdCallbackResponse {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @ApiModelProperty(value = "The entire case data to be returned with updated fields")
    private Map<String, Object> data;
    @ApiModelProperty(value = "Error messages")
    private List<String> errors;
    @ApiModelProperty(value = "Warning messages")
    private List<String> warnings;
    // Populate when you want to update a state of case.
    @ApiModelProperty(value = "State")
    private String state;

    public static Map<String, Object> convertToCcdFormat(CaseData caseData) {
        return objectMapper.convertValue(caseData, new TypeReference<Map<String, Object>>() {
        });
    }
}
