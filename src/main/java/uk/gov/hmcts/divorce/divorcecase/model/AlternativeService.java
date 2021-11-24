package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessBetaOnlyAccess;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlternativeService {

    @CCD(
        label = "Application date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedServiceApplicationDate;

    @CCD(
        label = "Service application type",
        hint = "What type of service application has been received?",
        typeOverride = FixedList,
        typeParameterOverride = "AlternativeServiceType"
    )
    private AlternativeServiceType alternativeServiceType;

    @CCD(
        label = "Added date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedServiceAddedDate;

    @CCD(
        label = "Service Application Granted"
    )
    private YesOrNo serviceApplicationGranted;

    @CCD(
        label = "Reason for refusal",
        typeOverride = TextArea
    )
    private String serviceApplicationRefusalReason;

    @CCD(
        label = "Application decision date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceApplicationDecisionDate;

    @CCD(
        label = "Deemed service date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deemedServiceDate;

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

    @CCD(
        label = "Here are your order details"
    )
    private OrderSummary servicePaymentFeeOrderSummary;

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {CaseworkerAccessBetaOnlyAccess.class})
    private Bailiff bailiff = new Bailiff();

    @SuppressWarnings("PMD")
    @JsonIgnore
    public AlternativeServiceOutcome getOutcome() {
        return AlternativeServiceOutcome.builder()
            .alternativeServiceType(this.getAlternativeServiceType())
            .receivedServiceApplicationDate(this.getReceivedServiceApplicationDate())
            .receivedServiceAddedDate(this.getReceivedServiceAddedDate())
            .alternativeServiceType(this.getAlternativeServiceType())
            .paymentMethod(this.getPaymentMethod())
            .serviceApplicationGranted(this.getServiceApplicationGranted())
            .serviceApplicationRefusalReason(this.getServiceApplicationRefusalReason())
            .serviceApplicationDecisionDate(this.getServiceApplicationDecisionDate())
            .deemedServiceDate(this.getDeemedServiceDate())
            .localCourtName(this.getBailiff().getLocalCourtName())
            .localCourtEmail(this.getBailiff().getLocalCourtEmail())
            .certificateOfServiceDocument(this.getBailiff().getCertificateOfServiceDocument())
            .certificateOfServiceDate(this.getBailiff().getCertificateOfServiceDate())
            .successfulServedByBailiff(this.getBailiff().getSuccessfulServedByBailiff())
            .reasonFailureToServeByBailiff(this.getBailiff().getReasonFailureToServeByBailiff())
            .build();
    }
}
