package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentItem {

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("volume")
    private String volume;

    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;

    @JsonProperty("memo_line")
    private String memoLine;

    @JsonProperty("natural_account_code")
    private String naturalAccountCode;

    @JsonProperty("code")
    private String code;

    @JsonProperty("calculated_amount")
    private String calculatedAmount;

    @JsonProperty("version")
    private String version;
}
