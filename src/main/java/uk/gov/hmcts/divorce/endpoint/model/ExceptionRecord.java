package uk.gov.hmcts.divorce.endpoint.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExceptionRecord {

    @JsonProperty("case_type_id")
    private String caseTypeId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("po_box")
    private String poBox;

    @JsonProperty("form_type")
    private String formType;

    @JsonProperty("scanned_documents")
    private List<InputScannedDoc> scannedDocuments;

    @JsonProperty("ocr_data_fields")
    private List<OcrDataField> ocrDataFields;

}
