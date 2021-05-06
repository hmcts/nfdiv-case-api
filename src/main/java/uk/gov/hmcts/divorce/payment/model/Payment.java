package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @CCD(
        label = "Payment Date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @CCD(
        label = "Payment Fee Id"
    )
    private String paymentFeeId;

    @CCD(
        label = "Payment Amount"
    )
    private Integer paymentAmount;

    @CCD(
        label = "Payment Site Id"
    )
    @ApiModelProperty("ID of site the payment was made")
    private String paymentSiteId;

    @CCD(
        label = "Payment Status"
    )
    private PaymentStatus paymentStatus;

    @CCD(
        label = "Payment Channel"
    )
    private String paymentChannel;

    @CCD(
        label = "Payment Reference"
    )
    private String paymentReference;

    @CCD(
        label = "Payment Transaction Id"
    )
    private String paymentTransactionId;
}
