package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class AlternativeService {

    @CCD(
        label = "Date of Payment",
        typeOverride = Date
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfPayment;

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
    private String feeAccountNumber;

    @CCD(
        label = "Enter your reference",
        hint = "This will appear on your statement to help you identify this payment"
    )
    private String feeAccountReferenceNumber;

    @CCD(
        label = "Help with Fees reference"
    )
    private String helpWithFeesReferenceNumber;
}
