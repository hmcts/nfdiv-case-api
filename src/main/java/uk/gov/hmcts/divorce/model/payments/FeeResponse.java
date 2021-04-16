package uk.gov.hmcts.divorce.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "The response from retrieving a fee from fees and payments service")
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
