package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class FeeDetails {

    @CCD(
        label = "Here are your order details"
    )
    private OrderSummary orderSummary;

    @CCD(
        label = "How will payment be made?",
        hint = "How will payment be made?",
        typeOverride = FixedList,
        typeParameterOverride = "ServicePaymentMethod"
    )
    private ServicePaymentMethod paymentMethod;

    @CCD(
        label = "Enter your account number",
        hint = "Example: PBA0896366"
    )
    private String accountNumber;

    @CCD(
        label = "Enter your reference"
    )
    private String accountReferenceNumber;

    @CCD(
        label = "Help with Fees reference"
    )
    private String helpWithFeesReferenceNumber;
}
