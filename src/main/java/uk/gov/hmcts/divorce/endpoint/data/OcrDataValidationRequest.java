package uk.gov.hmcts.divorce.endpoint.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkscan.data.KeyValue;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrDataValidationRequest {

    @NotEmpty
    @JsonProperty("ocr_data_fields")
    private List<ListValue<KeyValue>> ocrDataFields;
}
