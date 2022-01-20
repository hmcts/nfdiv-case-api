package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
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

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {CaseworkerAccessBetaOnlyAccess.class})
    private Bailiff bailiff = new Bailiff();

    @JsonUnwrapped(prefix = "servicePaymentFee")
    @Builder.Default
    @CCD(access = {CaseworkerAccessBetaOnlyAccess.class})
    private FeeDetails servicePaymentFee = new FeeDetails();

    @SuppressWarnings("PMD")
    @JsonIgnore
    public AlternativeServiceOutcome getOutcome() {
        return AlternativeServiceOutcome.builder()
            .alternativeServiceType(alternativeServiceType)
            .receivedServiceApplicationDate(receivedServiceApplicationDate)
            .receivedServiceAddedDate(receivedServiceAddedDate)
            .paymentMethod(servicePaymentFee.getPaymentMethod())
            .serviceApplicationGranted(serviceApplicationGranted)
            .serviceApplicationRefusalReason(serviceApplicationRefusalReason)
            .serviceApplicationDecisionDate(serviceApplicationDecisionDate)
            .deemedServiceDate(deemedServiceDate)
            .localCourtName(bailiff.getLocalCourtName())
            .localCourtEmail(bailiff.getLocalCourtEmail())
            .certificateOfServiceDocument(bailiff.getCertificateOfServiceDocument())
            .certificateOfServiceDate(bailiff.getCertificateOfServiceDate())
            .successfulServedByBailiff(bailiff.getSuccessfulServedByBailiff())
            .reasonFailureToServeByBailiff(bailiff.getReasonFailureToServeByBailiff())
            .build();
    }
}
