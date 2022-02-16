package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransformationDetails {
    private CaseData caseData;
    private OcrDataFields ocrDataFields;
}
