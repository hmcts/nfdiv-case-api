package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static joptsimple.internal.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

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

        final var expectedApplicant2 =
            jsonToObject("src/test/resources/transformation/output/applicant2-transformed.json", Applicant.class);

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

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review respondent/applicant2 first name",
                "Please review respondent/applicant2 last name",
                "Please review respondent/applicant2 name different to marriage certificate in the scanned form",
                "Please review respondent/applicant2 Address.  Country changed from '' to 'UK'.",
                "Please review applicant2 financial order for in scanned form",
                "Please review applicant2 financial order prayer for in scanned form"
            );

        final var expectedApplicant2 =
            jsonToObject("src/test/resources/transformation/output/applicant2-transformed-warnings.json", Applicant.class);

        assertThat(transformedOutput.getCaseData().getApplicant2())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant2);
    }

    @Test
    void shouldSuccessfullyTransformWithWarningsWhenFinancialOrderPrayerValueIsMissing() throws Exception {
        String validApplicant2OcrJson = loadJson("src/test/resources/transformation/input/valid-applicant2-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant2OcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setApplicant2FinancialOrder("Yes");
        ocrData.setApplicant2FinancialOrderFor("children");
        ocrData.setApplicant2PrayerFinancialOrder(EMPTY);

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant2Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder("Please review applicant2 financial order prayer for in scanned form");
        final var expectedApplicant2 =
            jsonToObject("src/test/resources/transformation/output/applicant2-transformed.json", Applicant.class);
        expectedApplicant2.setFinancialOrder(YES);
        expectedApplicant2.setFinancialOrdersFor(Set.of(CHILDREN));

        assertThat(transformedOutput.getCaseData().getApplicant2())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant2);
    }

    @Test
    void shouldSuccessfullyTransformWithWarningsWhenFinancialOrderPrayerValueIsAvailableAndFinancialOrderIsEmpty() throws Exception {
        String validApplicant2OcrJson = loadJson("src/test/resources/transformation/input/valid-applicant2-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant2OcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setApplicant2FinancialOrder("No");
        ocrData.setApplicant2FinancialOrderFor(EMPTY);
        ocrData.setApplicant2PrayerFinancialOrder("myself,children");

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant2Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder("Please review applicant2 financial order prayer for in scanned form");

        final var expectedApplicant2 =
            jsonToObject("src/test/resources/transformation/output/applicant2-transformed.json", Applicant.class);
        expectedApplicant2.setFinancialOrdersFor(emptySet());
        expectedApplicant2.setFinancialOrder(NO);

        assertThat(transformedOutput.getCaseData().getApplicant2())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant2);
    }
}
