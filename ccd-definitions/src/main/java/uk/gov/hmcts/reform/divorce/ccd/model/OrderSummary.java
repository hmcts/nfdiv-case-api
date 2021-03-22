package uk.gov.hmcts.reform.divorce.ccd.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode()
@Builder
public class OrderSummary {
    @JsonProperty("PaymentReference")
    @CCD(
        label = "Payment Reference"
    )
    private String paymentReference;

    @JsonProperty("PaymentTotal")
    @CCD(
        label = "Payment Total"
    )
    private String paymentTotal;

    @JsonProperty("Fees")
    @CCD(
        label = "Fees"
    )
    private List<FeeItem> fees;
}
