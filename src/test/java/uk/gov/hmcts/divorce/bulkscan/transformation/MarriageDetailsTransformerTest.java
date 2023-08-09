package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;


@ExtendWith(MockitoExtension.class)
public class MarriageDetailsTransformerTest {

    @InjectMocks
    private MarriageDetailsTransformer marriageDetailsTransformer;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldSuccessfullyTransformMarriageDetailsWithoutWarnings() throws Exception {
        String validMarriageDetailsOcrJson = loadJson("src/test/resources/transformation/input/valid-marriage-details-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validMarriageDetailsOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = marriageDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedMarriageDetails =
            jsonToObject("src/test/resources/transformation/output/marriage-details-transformed.json", MarriageDetails.class);

        assertThat(transformedOutput.getCaseData().getApplication().getScreenHasMarriageCert()).isEqualTo(YES);
        assertThat(transformedOutput.getCaseData().getApplication().getMarriageDetails())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedMarriageDetails);
    }

    @Test
    void shouldSuccessfullyTransformMarriageDetailsWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidOcrJson = loadJson("src/test/resources/transformation/input/invalid-marriage-details-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = marriageDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review marriage certificate/translation in the scanned form",
                "Please review applicant1's full name as on marriage cert in the scanned form",
                "Please review respondent/applicant2's full name as on marriage cert in the scanned form",
                "Please review married outside UK in the scanned form",
                "Please review making an application without marriage certificate in the scanned form",
                "Please review reasons why cert not correct in the scanned form"
            );

        final var expectedMarriageDetails =
            jsonToObject("src/test/resources/transformation/output/marriage-details-transformed-warnings.json", MarriageDetails.class);

        assertThat(transformedOutput.getCaseData().getApplication().getScreenHasMarriageCert()).isEqualTo(NO);
        assertThat(transformedOutput.getCaseData().getPaperFormDetails())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedMarriageDetails);
    }

    @Test
    void shouldSuccessfullyTransformMarriageDetailsWithWarningsWhenOcrContainsInvalidMarriageDateAndIncorrectCert() throws Exception {
        String invalidOcrJson = loadJson("src/test/resources/transformation/input/invalid-marriage-details-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidOcrJson, new TypeReference<>() {
        });

        OcrDataFields dataFields = transformOcrMapToObject(ocrDataFields);
        dataFields.setDetailsOnCertCorrect("Both");
        dataFields.setMarriageOutsideOfUK("No");
        dataFields.setDateOfMarriageOrCivilPartnershipMonth("invalid");

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(dataFields)
                .caseData(caseData)
                .build();

        final var transformedOutput = marriageDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getApplication().getScreenHasMarriageCert()).isEqualTo(NO);
        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review marriage certificate/translation in the scanned form",
                "Please review applicant1's full name as on marriage cert in the scanned form",
                "Please review respondent/applicant2's full name as on marriage cert in the scanned form",
                "Please review making an application without marriage certificate in the scanned form",
                "Please review marriage date in the scanned form",
                "Please review place of marriage or civil partnership in scanned form",
                "Please review marriage certificate details is correct in the scanned form"
            );

        final var expectedMarriageDetails =
            jsonToObject("src/test/resources/transformation/output/marriage-details-transformed-warnings.json", MarriageDetails.class);


        assertThat(transformedOutput.getCaseData().getPaperFormDetails())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedMarriageDetails);
    }

    @Test
    void shouldSuccessfullyTransformMarriageDetailsWithWarningsWhenMarriageMonthIsEmpty() throws Exception {
        String validMarriageDetailsOcrJson = loadJson("src/test/resources/transformation/input/valid-marriage-details-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validMarriageDetailsOcrJson, new TypeReference<>() {
        });

        OcrDataFields dataFields = transformOcrMapToObject(ocrDataFields);
        dataFields.setDateOfMarriageOrCivilPartnershipMonth("");

        final var caseData = CaseData.builder().build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(dataFields)
                .caseData(caseData)
                .build();

        final var transformedOutput = marriageDetailsTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please review marriage date in the scanned form"
            );


        final var expectedMarriageDetails =
            jsonToObject("src/test/resources/transformation/output/marriage-details-transformed.json", MarriageDetails.class);

        assertThat(transformedOutput.getCaseData().getApplication().getScreenHasMarriageCert()).isEqualTo(YES);
        assertThat(transformedOutput.getCaseData().getApplication().getMarriageDetails())
            .usingRecursiveComparison()
            .ignoringActualNullFields()
            .isEqualTo(expectedMarriageDetails);
    }
}
