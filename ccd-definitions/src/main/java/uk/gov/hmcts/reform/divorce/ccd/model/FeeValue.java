package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.math.BigDecimal;

@Data
@Builder
public class FeeValue {
    @JsonProperty("FeeDescription")
    @CCD(
        label = "Fee Description"
    )
    private String feeDescription;

    @JsonProperty("FeeVersion")
    @CCD(
        label = "Fee Version"
    )
    private String feeVersion;

    @JsonProperty("FeeCode")
    @CCD(
        label = "Fee Code"
    )
    private String feeCode;

    @JsonProperty("FeeAmount")
    @CCD(
        label = "Fee Amount"
    )
    private String feeAmount;

    public static String getValueInPence(double value) {
        return BigDecimal.valueOf(value).movePointRight(2).toPlainString();
    }


}
