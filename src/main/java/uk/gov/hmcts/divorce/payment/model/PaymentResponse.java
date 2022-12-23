package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponse {

    @JsonProperty("case_reference")
    private String caseReference;

    @JsonProperty("method")
    private String method;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("status")
    private String status;

    @JsonProperty("amount")
    private int amount;



}
