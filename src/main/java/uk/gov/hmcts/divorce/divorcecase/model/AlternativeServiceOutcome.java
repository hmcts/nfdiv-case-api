package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlternativeServiceOutcome {

    @CCD(
        label = "Application date",
        displayOrder = 1
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedServiceApplicationDate;

    @CCD(
        label = "Added date",
        displayOrder = 2
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedServiceAddedDate;

    @CCD(
        label = "What type of service application was received",
        typeParameterOverride = "AlternativeServiceType",
        displayOrder = 3
    )
    private AlternativeServiceType alternativeServiceType;

    @CCD(
        label = "How was payment made?",
        typeOverride = FixedList,
        typeParameterOverride = "ServicePaymentMethod",
        displayOrder = 4
    )
    private ServicePaymentMethod paymentMethod;

    @CCD(
        label = "Outcome of service application",
        displayOrder = 5
    )
    private String serviceApplicationOutcomeLabel;

    @CCD(
        label = "Service Application Granted",
        displayOrder = 6
    )
    private YesOrNo serviceApplicationGranted;

    /*
    Deemed or Dispensed Fields
     */
    @CCD(
        label = "Refusal reason",
        typeOverride = FixedRadioList,
        typeParameterOverride = "ServiceApplicationRefusalReason",
        displayOrder = 7
    )
    private ServiceApplicationRefusalReason refusalReason;

    @CCD(
        label = "Reason for refusal",
        typeOverride = TextArea,
        displayOrder = 8
    )
    private String serviceApplicationRefusalReason;

    @CCD(
        label = "Service Application Decision date",
        displayOrder = 9
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceApplicationDecisionDate;

    @CCD(
        label = "Deemed service date",
        displayOrder = 10
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deemedServiceDate;

    /*
    Bailiff Type Service Fields
     */
    @CCD(
        label = "Court name",
        displayOrder = 11
    )
    private String localCourtName;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        displayOrder = 12
    )
    private String localCourtEmail;

    @CCD(
        label = "Certificate of Service",
        displayOrder = 13
    )
    private DivorceDocument certificateOfServiceDocument;

    @CCD(
        label = "Certificate of service date",
        displayOrder = 14
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certificateOfServiceDate;

    @CCD(
        label = "Did bailiff serve successfully?",
        displayOrder = 15
    )
    private YesOrNo successfulServedByBailiff;

    @CCD(
        label = "Reason for failure to serve",
        typeOverride = TextArea,
        displayOrder = 16
    )
    private String reasonFailureToServeByBailiff;

    public String getServiceApplicationOutcomeLabel() {
        return " ";
    }

    @JsonIgnore
    public Optional<Document> getCertificateOfServiceDocumentLink() {
        return ofNullable(getCertificateOfServiceDocument())
            .map(DivorceDocument::getDocumentLink);
    }

    @JsonIgnore
    public boolean hasServiceApplicationBeenGranted() {
        return serviceApplicationGranted == YES;
    }

    @JsonIgnore
    public boolean hasBeenSuccessfullyServedByBailiff() {
        return successfulServedByBailiff == YES;
    }
}
