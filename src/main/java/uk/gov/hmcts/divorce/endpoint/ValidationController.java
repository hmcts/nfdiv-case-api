package uk.gov.hmcts.divorce.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.divorce.endpoint.data.*;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.apache.commons.lang3.EnumUtils.isValidEnum;

@RestController
public class ValidationController {

    @PostMapping(
        value = "/forms/{form-type}/validate-ocr",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OcrValidationResponse> validate(@PathVariable("form-type") final String formType,
                                                          @Valid @RequestBody final OcrDataValidationRequest requestBody) {

        String encodedFormType = UriUtils.encode(formType, StandardCharsets.UTF_8);
        if (!isValidEnum(FormType.class, encodedFormType)) {
            return ResponseEntity.ok().body(new OcrValidationResponse(
                Collections.emptyList(),
                Collections.singletonList("Form type '" + encodedFormType + "' not found"),
                ValidationStatus.ERRORS
            ));
        }

        // TODO:
        // Is s2s required
        // configure http controllers in config
        // Required: s2s token
        // Write handle validation function

        ValidationResult result = handleValidation(requestBody);

        return ResponseEntity.ok().body(new OcrValidationResponse(result.getWarnings(), result.getErrors(), result.getStatus()));
    }
}
