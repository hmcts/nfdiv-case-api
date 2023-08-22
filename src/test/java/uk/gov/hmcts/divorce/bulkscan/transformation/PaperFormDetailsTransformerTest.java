package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.PaperFormDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.PaperCasePaymentMethod.CHEQUE_OR_POSTAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.PaperCasePaymentMethod.PHONE;

@ExtendWith(MockitoExtension.class)
public class PaperFormDetailsTransformerTest {

    @InjectMocks
    private PaperFormDetailsTransformer paperFormDetailsTransformer;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyTransformPaperFormDetailsWithoutWarnings() throws Exception {
        String validPaperFormOcrJson = loadJson("src/test/resources/transformation/input/valid-paper-form-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validPaperFormOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = paperFormDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedPaperFormDetails =
            jsonToObject("src/test/resources/transformation/output/paper-form-transformed.json", PaperFormDetails.class);

        assertThat(transformedOutput.getCaseData().getPaperFormDetails())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedPaperFormDetails);
    }

    @Test
    void shouldSuccessfullyTransformPaperFormDetailsWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidPaperOcrJson = loadJson("src/test/resources/transformation/input/invalid-paper-form-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidPaperOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = paperFormDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review serve respondent outside UK in scanned form",
                "Please review statement of truth for applicant1 in scanned form",
                "Please review statement of truth signing for applicant1 in scanned form",
                "Please review sole or applicant1/legal representative name in scanned form",
                "Please review statement of truth date for applicant1 in scanned form",
                "Please review statement of truth for applicant2 in scanned form",
                "Please review statement of truth for applicant2 in scanned form",
                "Please sole or applicant2/legal representative name in scanned form",
                "Please review statement of truth date for applicant2 in scanned form"
            );

        final var expectedPaperForm =
            jsonToObject("src/test/resources/transformation/output/paper-form-transformed-warnings.json", PaperFormDetails.class);

        assertThat(transformedOutput.getCaseData().getPaperFormDetails())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedPaperForm);
    }

    @Test
    void shouldSetPaperCasePaymentMethodToChequeOrPostalOrderIfPhonePaymentMethodIsSelected() {
        List<OcrDataField> ocrDataFields = List.of(
            new OcrDataField("chequeOrPostalOrderPayment", "true"),
            new OcrDataField("debitCreditCardPaymentPhone", "false")
        );

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = paperFormDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getApplication().getPaperCasePaymentMethod())
            .isEqualTo(CHEQUE_OR_POSTAL_ORDER);
    }


    @Test
    void shouldSetPaperCasePaymentMethodToPhoneIfPhonePaymentMethodIsSelected() {
        List<OcrDataField> ocrDataFields = List.of(
            new OcrDataField("chequeOrPostalOrderPayment", "false"),
            new OcrDataField("debitCreditCardPaymentPhone", "true")
        );

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = paperFormDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getApplication().getPaperCasePaymentMethod())
            .isEqualTo(PHONE);
    }

    @Test
    void shouldNotSetPaperCasePaymentMethodIfNeitherPaymentMethodIsSelected() {
        List<OcrDataField> ocrDataFields = List.of(
            new OcrDataField("chequeOrPostalOrderPayment", "false"),
            new OcrDataField("debitCreditCardPaymentPhone", "false")
        );

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = paperFormDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getApplication().getPaperCasePaymentMethod()).isNull();
    }
}
