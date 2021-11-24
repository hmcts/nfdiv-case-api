package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Email;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

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
        label = "Reason for refusal",
        typeOverride = TextArea,
        displayOrder = 7
    )
    private String serviceApplicationRefusalReason;

    @CCD(
        label = "Service Application Decision date",
        displayOrder = 8
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceApplicationDecisionDate;

    @CCD(
        label = "Deemed service date",
        displayOrder = 9
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deemedServiceDate;

    /*
    Bailiff Type Service Fields
     */
    @CCD(
        label = "Court name",
        displayOrder = 10
    )
    private String localCourtName;

    @CCD(
        label = "Email address",
        typeOverride = Email,
        displayOrder = 11
    )
    private String localCourtEmail;

    @CCD(
        label = "Certificate of Service",
        displayOrder = 12
    )
    private DivorceDocument certificateOfServiceDocument;

    @CCD(
        label = "Certificate of service date",
        displayOrder = 13
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate certificateOfServiceDate;

    @CCD(
        label = "Did bailiff serve successfully?",
        displayOrder = 14
    )
    private YesOrNo successfulServedByBailiff;

    @CCD(
        label = "Reason for failure to serve",
        typeOverride = TextArea,
        displayOrder = 15
    )
    private String reasonFailureToServeByBailiff;

    public String getServiceApplicationOutcomeLabel() {
        return " ";
    }
}
