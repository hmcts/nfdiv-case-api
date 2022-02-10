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
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;

@ExtendWith(MockitoExtension.class)
public class Applicant1TransformerTest {
    @InjectMocks
    private Applicant1Transformer applicant1Transformer;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyTransformApplicant1DetailsWithoutWarnings() throws Exception {
        String validApplicant1OcrJson = loadJson("src/test/resources/transformation/input/valid-applicant1-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant1OcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant1Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings()).isEmpty();

        final var expectedApplicant1 =
            jsonToObject("src/test/resources/transformation/output/applicant1-transformed.json", Applicant.class);

        assertThat(transformedOutput.getCaseData().getApplicant1())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant1);
    }

    @Test
    void shouldSuccessfullyTransformApplicant1DetailsWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidApplicant1OcrJson = loadJson("src/test/resources/transformation/input/invalid-applicant1-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidApplicant1OcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant1Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 name different to marriage certificate in the scanned form",
                "Please review applicant1 solicitor name in the scanned form",
                "Please review applicant1 solicitor firm in the scanned form",
                "Please review applicant1 financial for in scanned form",
                "Please review existing or previous court cases in the scanned form"
            );

        final var expectedApplicant1 =
            jsonToObject("src/test/resources/transformation/output/applicant1-transformed-warnings.json", Applicant.class);

        assertThat(transformedOutput.getCaseData().getApplicant1())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant1);
    }

    @Test
    void shouldSuccessfullyTransformApplicant1DetailsWithWarningsWhenOcrContainsInvalidSolicitorDetails() throws Exception {
        String validApplicant1OcrJson = loadJson("src/test/resources/transformation/input/invalid-applicant1-solicitor-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant1OcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant1Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 solicitor details in the scanned form"
            );

        final var expectedApplicant1 =
            jsonToObject(
                "src/test/resources/transformation/output/applicant1-transformed-invalid-sol-warnings.json",
                Applicant.class
            );

        assertThat(transformedOutput.getCaseData().getApplicant1())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant1);
    }
}
