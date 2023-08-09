package uk.gov.hmcts.divorce.bulkscan.endpoint.model.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformationInput {

    @JsonProperty("case_type_id")
    private String caseTypeId;

    // Deprecated and is available to support older  version
    @JsonProperty("id")
    private String id;

    @JsonProperty("exception_record_id")
    private String exceptionRecordId;

    @JsonProperty("po_box")
    private String poBox;

    @JsonProperty("form_type")
    private String formType;

    @JsonProperty("scanned_documents")
    private List<InputScannedDoc> scannedDocuments;

    @JsonProperty("ocr_data_fields")
    private List<OcrDataField> ocrDataFields;

    @JsonProperty("is_automated_process")
    private boolean automatedProcessCreation;

    @JsonProperty("envelope_id")
    private String envelopeId;

}
