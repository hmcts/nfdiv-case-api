package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.access.CaseworkerAccessOnlyAccess;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Date;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;

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
        label = "Refusal reason",
        typeOverride = FixedRadioList,
        typeParameterOverride = "ServiceApplicationRefusalReason"
    )
    private ServiceApplicationRefusalReason refusalReason;

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
        label = "Further details for Judge or Legal Advisor",
        typeOverride = TextArea
    )
    private String alternativeServiceJudgeOrLegalAdvisorDetails;

    @CCD(
        label = "Is fee payment required?"
    )
    private YesOrNo alternativeServiceFeeRequired;

    @JsonUnwrapped
    @Builder.Default
    @CCD(access = {CaseworkerAccessOnlyAccess.class})
    private Bailiff bailiff = new Bailiff();

    @JsonUnwrapped(prefix = "servicePaymentFee")
    @Builder.Default
    @CCD(access = {CaseworkerAccessOnlyAccess.class})
    private FeeDetails servicePaymentFee = new FeeDetails();

    @CCD(
        label = "Supporting Documents",
        typeOverride = Collection,
        typeParameterOverride = "DivorceDocument"
    )
    private List<ListValue<DivorceDocument>> serviceApplicationDocuments;

    @SuppressWarnings("PMD")
    @JsonIgnore
    public AlternativeServiceOutcome getOutcome() {
        return AlternativeServiceOutcome.builder()
            .alternativeServiceType(alternativeServiceType)
            .receivedServiceApplicationDate(receivedServiceApplicationDate)
            .receivedServiceAddedDate(receivedServiceAddedDate)
            .paymentMethod(servicePaymentFee.getPaymentMethod())
            .serviceApplicationGranted(serviceApplicationGranted)
            .refusalReason(refusalReason)
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

    @JsonIgnore
    public boolean isApplicationGranted() {
        return YES.equals(serviceApplicationGranted);
    }

    @JsonIgnore
    public boolean isApplicationGrantedDeemedOrDispensed() {
        return YES.equals(serviceApplicationGranted) && DEEMED.equals(alternativeServiceType) || DISPENSED.equals(alternativeServiceType);
    }
}
