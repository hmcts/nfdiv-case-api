package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Optional;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CreditAccountPaymentRequest {

    private String amount;

    private String currency;

    @JsonProperty("customer_reference")
    private String customerReference;

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("idempotency_key")
    private String idempotencyKey;

    @JsonProperty("organisation_name")
    private String organisationName;

    public void setAmount(String amount) {
        this.amount = Optional.ofNullable(amount)
                .map(Double::parseDouble).map(i -> i / 100)
                .map(String::valueOf).orElse(amount);
    }

}
