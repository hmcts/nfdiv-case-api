package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransformationDetails {
    private CaseData caseData;
    private OcrDataFields ocrDataFields;
    @Builder.Default
    private List<String> transformationWarnings = new ArrayList<>();
}
