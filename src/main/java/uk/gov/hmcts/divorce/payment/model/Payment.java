package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class Payment {
    @JsonProperty("PaymentDate")
    private LocalDate paymentDate;

    @JsonProperty("PaymentFeeId")
    private String paymentFeeId;

    @JsonProperty("PaymentAmount")
    private String paymentAmount;

    @ApiModelProperty("ID of site the payment was made")
    @JsonProperty("PaymentSiteId")
    private String paymentSiteId;

    @JsonProperty("PaymentStatus")
    private PaymentStatus paymentStatus;

    @JsonProperty("PaymentChannel")
    private String paymentChannel;

    @JsonProperty("PaymentReference")
    private String paymentReference;

    @JsonProperty("PaymentTransactionId")
    private String paymentTransactionId;
}
