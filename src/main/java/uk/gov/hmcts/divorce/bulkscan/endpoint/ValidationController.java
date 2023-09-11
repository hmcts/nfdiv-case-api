package uk.gov.hmcts.divorce.bulkscan.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.ValidationStatus;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;

import java.nio.charset.StandardCharsets;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.EnumUtils.isValidEnum;

@RestController
@Slf4j
public class ValidationController {

    @Autowired
    private OcrValidator validator;

    @PostMapping(
        value = "/forms/{form-type}/validate-ocr",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<OcrValidationResponse> validate(@PathVariable("form-type") final String formType,
                                                          @RequestBody(required = false) final OcrDataValidationRequest requestBody) {

        String encodedFormType = UriUtils.encode(formType, StandardCharsets.UTF_8);
        if (!isValidEnum(FormType.class, encodedFormType)) {
            return ResponseEntity.ok().body(new OcrValidationResponse(
                emptyList(),
                singletonList("Form type '" + encodedFormType + "' not found"),
                ValidationStatus.ERRORS
            ));
        }

        OcrValidationResponse result = validator.validateExceptionRecord(encodedFormType, requestBody);
        log.info(valueOf(result));

        return ResponseEntity.ok().body(result);
    }
}
