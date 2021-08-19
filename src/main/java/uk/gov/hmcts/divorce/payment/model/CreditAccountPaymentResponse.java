package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.ccd.sdk.type.Fee;

import java.util.List;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditAccountPaymentResponse {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("ccd_case_number")
    private String ccdCaseNumber;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("fees")
    private List<Fee> fees;

    @JsonProperty("date_updated")
    private String dateUpdated;

    @JsonProperty("method")
    private String method;

    @JsonProperty("status_histories")
    private List<StatusHistoriesItem> statusHistories;

    @JsonProperty("date_created")
    private String dateCreated;

    @JsonProperty("service_name")
    private String serviceName;

    @JsonProperty("channel")
    private String channel;

    @JsonProperty("description")
    private String description;

    @JsonProperty("organisation_name")
    private String organisationName;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("external_provider")
    private String externalProvider;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("case_reference")
    private String caseReference;

    @JsonProperty("customer_reference")
    private String customerReference;

    @JsonProperty("external_reference")
    private String externalReference;

    @JsonProperty("site_id")
    private String siteId;

    @JsonProperty("payment_group_reference")
    private String paymentGroupReference;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;
}
