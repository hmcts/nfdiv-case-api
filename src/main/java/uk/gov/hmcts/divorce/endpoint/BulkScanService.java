package uk.gov.hmcts.divorce.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;

import java.util.Map;

@Service
public class BulkScanService {

    @Autowired
    private BulkScanFormTransformerFactory bulkScanFormTransformerFactory;

    public Map<String, Object> transformBulkScanForm(ExceptionRecord exceptionRecord) {
        // TODO: validate exception record for mandatory fields
        BulkScanFormTransformer bulkScanFormTransformer = bulkScanFormTransformerFactory.getTransformer(exceptionRecord.getFormType());
        return bulkScanFormTransformer.transformIntoCaseData(exceptionRecord);
    }
}
