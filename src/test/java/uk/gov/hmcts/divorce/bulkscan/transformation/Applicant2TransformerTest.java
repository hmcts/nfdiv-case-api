package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;

@ExtendWith(MockitoExtension.class)
public class Applicant2TransformerTest {
    @InjectMocks
    private Applicant2Transformer applicant2Transformer;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyTransformApplicant2DetailsWithoutWarnings() throws Exception {
        String validApplicant2OcrJson = loadJson("src/test/resources/transformation/input/valid-applicant2-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant2OcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant2Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings()).isEmpty();

        final var expectedApplicant2 =
            jsonToObject("src/test/resources/transformation/output/applicant2-transformed.json", Applicant.class);
        expectedApplicant2.setOffline(NO);

        assertThat(transformedOutput.getCaseData().getApplicant2())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant2);
    }

    @Test
    void shouldSuccessfullyTransformApplicant2DetailsWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidApplicant2OcrJson = loadJson("src/test/resources/transformation/input/invalid-applicant2-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidApplicant2OcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant2Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings())
            .containsExactlyInAnyOrder(
                "Please review respondent/applicant2 first name",
                "Please review respondent/applicant2 last name",
                "Please review respondent/applicant2 name different to marriage certificate in the scanned form",
                "Please review applicant2 financial order for in scanned form"
            );

        final var expectedApplicant2 =
            jsonToObject("src/test/resources/transformation/output/applicant2-transformed-warnings.json", Applicant.class);
        expectedApplicant2.setOffline(NO);

        assertThat(transformedOutput.getCaseData().getApplicant2())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant2);
    }
}
