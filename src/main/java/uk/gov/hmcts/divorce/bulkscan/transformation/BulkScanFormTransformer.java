package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.divorce.endpoint.model.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.bsp.common.config.BspCommonFields.BULK_SCAN_CASE_REFERENCE;

@Slf4j
public abstract class BulkScanFormTransformer {

    public Map<String, Object> transformIntoCaseData(ExceptionRecord exceptionRecord) {
        List<OcrDataField> ocrDataFields = exceptionRecord.getOcrDataFields();

        Map<String, Object> caseData = new HashMap<>();

        // Need to store the Exception Record ID as part of the CCD data
        caseData.put(BULK_SCAN_CASE_REFERENCE, exceptionRecord.getId());

        Map<String, Object> formSpecificMap = runFormSpecificTransformation(ocrDataFields);
        caseData.putAll(formSpecificMap);

        return caseData;
    }

    abstract Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFields);
}
