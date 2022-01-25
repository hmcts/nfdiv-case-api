package uk.gov.hmcts.divorce.bulkscan.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Label;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ExceptionRecord {

    @CCD(
        label = "# Envelope meta data",
        hint = "From scan envelope header",
        typeOverride = Label
    )
    private String envelopeLabel;

    @CCD(
        label = "Journey classification",
        hint = "Was this a supplementary evidence / new case or exception"
    )
    private String journeyClassification;

    @CCD(
        label = "PO box"
    )
    private String poBox;

    @CCD(
        label = "Jurisdiction"
    )
    private String poBoxJurisdiction;

    @CCD(
        label = "Delivery date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime deliveryDate;

    @CCD(
        label = "Opening date"
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime openingDate;

    @CCD(
        label = "Scanned documents",
        typeOverride = Collection,
        typeParameterOverride = "ScannedDocument"
    )
    private List<ListValue<ExceptionRecordScannedDocument>> scannedDocuments;

    @CCD(
        label = "Form OCR data",
        typeOverride = Collection,
        typeParameterOverride = "KeyValue"
    )
    private List<ListValue<KeyValue>> scanOCRData;

    @CCD(
        label = "Attach to case reference",
        hint = "The number of the case to attach the envelop to"
    )
    private String attachToCaseReference;

    @CCD(
        label = "Case reference",
        hint = "Reference number of the new case created from exception record"
    )
    private String caseReference;

    @CCD(
        label = "OCR data validation warnings",
        typeOverride = Collection,
        typeParameterOverride = "TextArea"
    )
    private List<String> ocrDataValidationWarnings;

    @CCD(
        label = "Display warnings",
        hint = "Indicates if warnings tab should be displayed"
    )
    private YesOrNo displayWarnings;

    @CCD(
        label = "Form type"
    )
    private String formType;

    @CCD(
        label = "Envelope Id",
        hint = "Id of the envelope the exception record was created from"
    )
    private String envelopeId;

    @CCD(
        label = "Awaiting Payment DCN processing",
        hint = "Indicates if the payment document control numbers are being processed"
    )
    private YesOrNo awaitingPaymentDCNProcessing;

    @CCD(
        label = "Contains payments",
        hint = "Indicates if the exception record contains payments"
    )
    private YesOrNo containsPayments;

    @CCD(
        label = "Envelope Id",
        hint = "Id of the envelope the exception record was created from"
    )
    private String envelopeCaseReference;

    @CCD(
        label = "Envelope legacy case reference",
        hint = "The legacy case reference number received in the envelope"
    )
    private String envelopeLegacyCaseReference;

    @CCD(
        label = "Display envelope case reference",
        hint = "Indicates if the envelope case reference field should be displayed"
    )
    private YesOrNo showEnvelopeCaseReference;

    @CCD(
        label = "Display envelope legacy case reference",
        hint = "Indicates if the envelope case reference field should be displayed"
    )
    private YesOrNo showEnvelopeLegacyCaseReference;

    @CCD(
        label = "Surname",
        hint = "Surname"
    )
    private String surname;

    @CCD(
        label = "Case Reference",
        hint = "The case reference to attach the envelope to"
    )
    private String searchCaseReference;
}
