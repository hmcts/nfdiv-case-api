package uk.gov.hmcts.reform.divorce.caseapi.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response from retrieving a fee from fees and payments service")
@Builder
public class FeeResponse {
    @ApiModelProperty(value = "The fee identifier")
    @JsonProperty("code")
    private String feeCode;
    @ApiModelProperty(value = "The fee amount in pounds")
    @JsonProperty("fee_amount")
    private Double amount;
    @ApiModelProperty(value = "The fee version")
    private Integer version;
    @ApiModelProperty(value = "The fee description")
    private String description;
}
