package uk.gov.hmcts.divorce.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonNaming(SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentCallbackDto {

    private String serviceRequestReference;

    private String ccdCaseNumber;

    private BigDecimal serviceRequestAmount;

    private ServiceRequestStatus serviceRequestStatus;

    private PaymentDto payment;

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonInclude(NON_NULL)
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Builder
    public static class PaymentDto {
        private BigDecimal paymentAmount;

        private String paymentReference;

        private OnlinePaymentMethod paymentMethod;

        private String accountNumber;

        private String caseReference;
    }

}
