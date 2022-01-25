package uk.gov.hmcts.divorce.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.endpoint.data.FormType;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.endpoint.data.ValidationStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javax.validation.Valid;

import static org.apache.commons.lang3.EnumUtils.isValidEnum;

@RestController
public class ValidationController {

    @Autowired
    private OcrValidator validator;

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

        OcrValidationResponse result = validator.validateExceptionRecord(requestBody);

        return ResponseEntity.ok().body(result);
    }
}
