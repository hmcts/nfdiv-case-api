package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.data.KeyValue;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.endpoint.data.ValidationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.WARNINGS;

@Component
@Slf4j
public class OcrValidator {


    public OcrValidationResponse validateExceptionRecord(OcrDataValidationRequest ocrDataValidationRequest) {

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Object> data = transformData(ocrDataValidationRequest.getOcrDataFields());

        return OcrValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .status(getValidationStatus(errors, warnings))
            .build();
    }

    private void checkApplicationType() {

    }

    private static ValidationStatus getValidationStatus(List<String> errors, List<String> warnings) {
        if (!ObjectUtils.isEmpty(errors)) {
            return ERRORS;
        }
        if (!ObjectUtils.isEmpty(warnings)) {
            return WARNINGS;
        }
        return ValidationStatus.SUCCESS;
    }

    private Map<String, Object> transformData(List<KeyValue> ocrDataFields) {
        return ocrDataFields.stream()
            .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
    }
}
