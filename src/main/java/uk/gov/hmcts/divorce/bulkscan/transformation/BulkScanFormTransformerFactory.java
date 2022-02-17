package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bsp.common.error.UnsupportedFormTypeException;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;

@Component
public class BulkScanFormTransformerFactory {

    @Autowired
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    private static final Map<String, BulkScanFormTransformer> bulkScanFormTransformerMap = new HashMap<>();

    @PostConstruct
    public void init() {
        bulkScanFormTransformerMap.put(D8.getName(), d8FormToCaseTransformer);
    }

    public BulkScanFormTransformer getTransformer(String formType) {
        if (!bulkScanFormTransformerMap.containsKey(formType)) {
            throw new UnsupportedFormTypeException(formType);
        }

        return bulkScanFormTransformerMap.get(formType);
    }
}
