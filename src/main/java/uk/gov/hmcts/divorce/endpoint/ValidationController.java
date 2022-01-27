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

import java.nio.charset.StandardCharsets;
import javax.validation.Valid;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;

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
                emptyList(),
                singletonList("Form type '" + encodedFormType + "' not found"),
                ERRORS
            ));
        }

        OcrValidationResponse result = validator.validateExceptionRecord(requestBody);

        return ResponseEntity.ok().body(result);
    }
}
