package uk.gov.hmcts.divorce.bulkscan.transformation;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.exception.UnsupportedFormTypeException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8S;

@Component
@RequiredArgsConstructor
public class BulkScanFormTransformerFactory {

    private final D8FormToCaseTransformer d8FormToCaseTransformer;

    private final D8sFormToCaseTransformer d8SFormToCaseTransformer;

    private static final Map<String, BulkScanFormTransformer> bulkScanFormTransformerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        bulkScanFormTransformerMap.put(D8.getName(), d8FormToCaseTransformer);
        bulkScanFormTransformerMap.put(D8S.getName(), d8SFormToCaseTransformer);
    }

    public BulkScanFormTransformer getTransformer(String formType) {
        if (!bulkScanFormTransformerMap.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return bulkScanFormTransformerMap.get(formType);
    }
}
