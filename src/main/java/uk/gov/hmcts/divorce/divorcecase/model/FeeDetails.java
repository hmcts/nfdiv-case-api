package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod.FEE_PAY_BY_ACCOUNT;

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
        label = "Select your account number"
    )
    private DynamicList pbaNumbers;

    @CCD(
        label = "Enter your reference"
    )
    private String accountReferenceNumber;

    @CCD(
        label = "Help with Fees reference"
    )
    private String helpWithFeesReferenceNumber;

    @JsonIgnore
    public boolean isPaymentMethodPba() {
        return FEE_PAY_BY_ACCOUNT.equals(this.getPaymentMethod());
    }

    @JsonIgnore
    public String getPbaNumber() {
        return this.getPbaNumbers().getValue().getLabel();
    }
}
