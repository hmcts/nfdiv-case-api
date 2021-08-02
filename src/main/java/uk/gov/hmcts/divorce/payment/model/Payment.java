package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @CCD(
        label = "Created date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @CCD(
        label = "Updated date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    @CCD(
        label = "fee code"
    )
    private String feeCode;

    @CCD(
        label = "Amount in pounds"
    )
    private Integer amount;

    @CCD(
        label = "Status"
    )
    private PaymentStatus status;

    @CCD(
        label = "Channel"
    )
    private String channel;

    @CCD(
        label = "Reference"
    )
    private String reference;

    @CCD(
        label = "Transaction Id"
    )
    private String transactionId;
}
