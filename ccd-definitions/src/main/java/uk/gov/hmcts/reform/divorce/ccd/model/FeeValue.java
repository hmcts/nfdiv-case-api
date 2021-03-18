package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FeeValue {
    @JsonProperty("FeeDescription")
    private String feeDescription;

    @JsonProperty("FeeVersion")
    private String feeVersion;

    @JsonProperty("FeeCode")
    private String feeCode;

    @JsonProperty("FeeAmount")
    private String feeAmount;

    public static String getValueInPence(double value) {
        return BigDecimal.valueOf(value).movePointRight(2).toPlainString();
    }


}
