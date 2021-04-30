package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.MoneyGBP;
import uk.gov.hmcts.divorce.common.model.access.DefaultAccess;

import java.time.LocalDate;

@Data
@Builder
public class Payment {

    @CCD(
        label = "Payment Date",
        access = {DefaultAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @CCD(
        label = "Payment Fee Id",
        access = {DefaultAccess.class}
    )
    private String paymentFeeId;

    @CCD(
        label = "Payment Amount",
        access = {DefaultAccess.class}
    )
    private MoneyGBP paymentAmount;

    @CCD(
        label = "Payment Site Id",
        access = {DefaultAccess.class}
    )
    @ApiModelProperty("ID of site the payment was made")
    private String paymentSiteId;

    @CCD(
        label = "Payment Status",
        access = {DefaultAccess.class}
    )
    private PaymentStatus paymentStatus;

    @CCD(
        label = "Payment Channel",
        access = {DefaultAccess.class}
    )
    private String paymentChannel;

    @CCD(
        label = "Payment Reference",
        access = {DefaultAccess.class}
    )
    private String paymentReference;

    @CCD(
        label = "Payment Transaction Id",
        access = {DefaultAccess.class}
    )
    private String paymentTransactionId;
}
