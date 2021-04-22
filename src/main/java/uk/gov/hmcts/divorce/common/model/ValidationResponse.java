package uk.gov.hmcts.divorce.common.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@ApiModel(description = "The response to case validation")
@Builder
public class ValidationResponse {

    private String validationStatus;

    private List<String> errors;

    private List<String> warnings;

}
