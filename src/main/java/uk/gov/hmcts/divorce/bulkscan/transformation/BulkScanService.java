package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.TransformationInput;

import java.util.Map;

@Service
public class BulkScanService {

    @Autowired
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public Map<String, Object> transformBulkScanForm(TransformationInput transformationInput) {
        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(transformationInput.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(transformationInput);
    }
}
