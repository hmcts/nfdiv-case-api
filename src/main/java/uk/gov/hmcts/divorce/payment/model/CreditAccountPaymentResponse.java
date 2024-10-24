package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditAccountPaymentResponse {

    @JsonProperty("payment_reference")
    private String paymentReference;

    private int amount;

    private String currency;

    @JsonProperty("customer_reference")
    private String customerReference;

    @JsonProperty("account_number")
    private String accountNumber;

    private String status;

    @JsonProperty("status_histories")
    private List<StatusHistoriesItem> statusHistories;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("organisation_name")
    private String organisationName;
}
