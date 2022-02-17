package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.endpoint.model.ExceptionRecord;

import java.util.Map;

@Service
public class BulkScanService {

    @Autowired
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) {
        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }
}
