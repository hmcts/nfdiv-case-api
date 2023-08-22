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

import static java.util.Collections.emptySet;
import static joptsimple.internal.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
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

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

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

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 name different to marriage certificate in the scanned form",
                "Please review applicant1 solicitor name in the scanned form",
                "Please review applicant1 solicitor firm in the scanned form",
                "Please review applicant1 financial order for in scanned form",
                "Please review existing or previous court cases in the scanned form",
                "Please review applicant1 financial order prayer for in scanned form"
            );

        final var expectedApplicant1 =
            jsonToObject("src/test/resources/transformation/output/applicant1-transformed-warnings.json", Applicant.class);

        assertThat(transformedOutput.getCaseData().getApplicant1())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant1);
    }

    @Test
    void shouldSuccessfullyTransformApplicant1DetailsWithWarningsWhenOcrContainsMissingSolicitorDetails() throws Exception {
        String validApplicant1OcrJson = loadJson("src/test/resources/transformation/input/invalid-applicant1-solicitor-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant1OcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setSoleOrApplicant1Solicitor("Yes");
        ocrData.setSoleOrApplicant1SolicitorName(EMPTY);
        ocrData.setSoleOrApplicant1SolicitorFirm(EMPTY);
        ocrData.setSoleOrApplicant1BuildingAndStreet(EMPTY);
        ocrData.setExistingOrPreviousCourtCases("Both");
        ocrData.setSoleOrApplicant1FinancialOrder("Yes");
        ocrData.setSoleOrApplicant1FinancialOrderFor(EMPTY);

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant1Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 solicitor name in the scanned form",
                "Please review applicant1 solicitor firm in the scanned form",
                "Please review applicant1 solicitor building and street in the scanned form",
                "Please review applicant1 financial order for in scanned form",
                "Please review applicant1 financial order prayer for in scanned form",
                "Please review existing or previous court cases in the scanned form"
            );

        final var expectedApplicant1 =
            jsonToObject(
                "src/test/resources/transformation/output/applicant1-transformed-invalid-sol-warnings.json",
                Applicant.class
            );
        expectedApplicant1.setFinancialOrdersFor(emptySet());
        expectedApplicant1.setSolicitorRepresented(YES);
        expectedApplicant1.getSolicitor().setAddress("some street\nsecond line of address\nUK");
        expectedApplicant1.getSolicitor().setEmail("testsol@dummy.com");
        expectedApplicant1.getSolicitor().setFirmName(EMPTY);
        expectedApplicant1.getSolicitor().setName(EMPTY);
        expectedApplicant1.getSolicitor().setPhone(EMPTY);
        expectedApplicant1.getAddress().setAddressLine1(EMPTY);

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

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 solicitor details in the scanned form",
                "Please review applicant1 financial order prayer for in scanned form"
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

    @Test
    void shouldSuccessfullyTransformWithWarningsWhenFinancialOrderPrayerValueIsMissing() throws Exception {
        String validApplicant1OcrJson = loadJson("src/test/resources/transformation/input/valid-applicant1-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant1OcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setSoleOrApplicant1FinancialOrder("Yes");
        ocrData.setSoleOrApplicant1prayerFinancialOrder(EMPTY);

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant1Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 financial order prayer for in scanned form"
            );

        final var expectedApplicant1 =
            jsonToObject("src/test/resources/transformation/output/applicant1-transformed.json", Applicant.class);

        assertThat(transformedOutput.getCaseData().getApplicant1())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant1);
    }


    @Test
    void shouldSuccessfullyTransformWithWarningsWhenFinancialOrderPrayerValueIsAvailableAndFinancialOrderIsEmpty() throws Exception {
        String validApplicant1OcrJson = loadJson("src/test/resources/transformation/input/valid-applicant1-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicant1OcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setSoleOrApplicant1FinancialOrder("No");
        ocrData.setSoleOrApplicant1FinancialOrderFor(EMPTY);
        ocrData.setSoleOrApplicant1prayerFinancialOrder("myself,children");

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicant1Transformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review applicant1 financial order prayer for in scanned form"
            );

        final var expectedApplicant1 =
            jsonToObject("src/test/resources/transformation/output/applicant1-transformed.json", Applicant.class);
        expectedApplicant1.setFinancialOrdersFor(emptySet());
        expectedApplicant1.setFinancialOrder(NO);

        assertThat(transformedOutput.getCaseData().getApplicant1())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedApplicant1);
    }
}
