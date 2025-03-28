package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.TransformationInput;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BulkScanService {

    private final BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public Map<String, Object> transformBulkScanForm(TransformationInput transformationInput) {
        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(transformationInput.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(transformationInput);
    }
}
