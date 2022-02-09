package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class D8FormToCaseTransformerTest {
    @Autowired
    private D8FormToCaseTransformer d8FormToCaseTransformer;

    @Autowired
    private OcrValidator validator;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @Test
    @Disabled
    void shouldSuccessfullyTransformD8FormWithoutWarnings() throws Exception {
        String validOcrJson = Files.readString(Paths.get("src/test/resources/transformation/valid-ocr.json"), UTF_8);
        List<OcrDataField> ocrDataFields = mapper.readValue(validOcrJson, new TypeReference<>() {
        });

        var exceptionRecord = ExceptionRecord
            .builder()
            .ocrDataFields(ocrDataFields)
            .formType(D8.getName())
            .build();

        when(validator.validateOcrData(D8.getName(), transformOcrMapToObject(ocrDataFields)))
            .thenReturn(OcrValidationResponse.builder().build());

        Map<String, Object> transformedData = d8FormToCaseTransformer.transformIntoCaseData(exceptionRecord);
    }
}
