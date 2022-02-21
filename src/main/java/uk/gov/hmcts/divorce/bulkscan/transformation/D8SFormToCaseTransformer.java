package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class D8SFormToCaseTransformer extends BulkScanFormTransformer {

    @Override
    Map<String, Object> runFormSpecificTransformation(final List<OcrDataField> ocrDataFields) {
        return null;
    }
}
