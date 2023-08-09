package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.TRANSFORMATION_AND_OCR_WARNINGS;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;

@ExtendWith(MockitoExtension.class)
public class CommonFormToCaseTransformerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Spy
    private ObjectMapper mockObjectMapper = MAPPER;

    @InjectMocks
    private CommonFormToCaseTransformer commonFormToCaseTransformer;

    @Test
    void verifyFieldsShouldReturnNoTransformationWarningsWhenValidDataIsGiven() throws Exception {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8-form-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        List<String> transformationWarnings = new ArrayList<>();

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        transformationWarnings = commonFormToCaseTransformer.verifyFields(transformationDetails, transformationWarnings);

        assertThat(transformationWarnings).isEmpty();
    }

    @Test
    void verifyFieldsShouldReturnTransformationWarningsWhenInvalidDataIsGiven() throws Exception {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/invalid-d8-form-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        List<String> transformationWarnings = new ArrayList<>();

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        transformationWarnings = commonFormToCaseTransformer.verifyFields(transformationDetails, transformationWarnings);

        assertThat(transformationWarnings).isNotEmpty();

        List<String> expectedWarnings =
            Arrays.asList("Please review serve out of UK in the scanned form",
                "Please review respondent by post and applicant will serve application in the scanned form",
                "Please review respondent address different to service address in the scanned form"
            );

        assertThat(transformationWarnings).isEqualTo(expectedWarnings);
    }

    @Test
    void setDefaultValuesShouldReturnCaseDataCorrectlyPopulatedWhenValidDataGivenSole() {
        var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();

        caseData = commonFormToCaseTransformer.setDefaultValues(caseData);

        assertThat(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
    }

    @Test
    void setDefaultValuesShouldReturnCaseDataCorrectlyPopulatedWhenValidDataGivenJoint() {
        var caseData = CaseData.builder().applicationType(JOINT_APPLICATION).divorceOrDissolution(DISSOLUTION).build();

        caseData = commonFormToCaseTransformer.setDefaultValues(caseData);

        assertThat(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isEqualTo(NO);

        assertThat(caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isEqualTo(NO);
    }

    @Test
    void getApplicationTypeReturnsJointWhenSoleApplicationIsTicked() throws IOException {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8-form-ocr.json");
        List<OcrDataField> ocrDataFieldList = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataFieldList);

        List<String> warnings = new ArrayList<>();

        var applicationType = commonFormToCaseTransformer.getApplicationType(ocrDataFields, warnings);

        assertThat(applicationType).isEqualTo(SOLE_APPLICATION);
    }

    @Test
    void getApplicationTypeReturnsJointWhenJointApplicationIsTicked() throws IOException {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8-form-ocr.json");
        List<OcrDataField> ocrDataFieldList = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataFieldList);
        ocrDataFields.setSoleApplication("false");
        ocrDataFields.setJointApplication("true");

        List<String> warnings = new ArrayList<>();

        var applicationType = commonFormToCaseTransformer.getApplicationType(ocrDataFields, warnings);

        assertThat(applicationType).isEqualTo(JOINT_APPLICATION);
    }

    @Test
    void transformCaseDataReturnsSuccessfullyTransformedCaseDataWhenValidDataGiven() {

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();
        List<String> transformationWarnings = new ArrayList<>();

        var transformedCaseData = commonFormToCaseTransformer.transformCaseData(
            caseData,
            transformationWarnings);

        assertThat(transformedCaseData).contains(entry(TRANSFORMATION_AND_OCR_WARNINGS, emptyList()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void transformCaseDataReturnsWarningsWhenInvalidDataGiven() {

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();
        List<String> transformationWarnings = List.of(
            "Please review serve out of UK in the scanned form",
            "Please review respondent by post and applicant will serve application in the scanned form",
            "Please review respondent address different to service address in the scanned form"
        );

        var transformedCaseData = commonFormToCaseTransformer.transformCaseData(
            caseData,
            transformationWarnings);

        final List<ListValue<String>> extractedWarnings =
            (List<ListValue<String>>) transformedCaseData.get(TRANSFORMATION_AND_OCR_WARNINGS);

        assertThat(extractedWarnings)
            .extracting("value")
            .isEqualTo(
                List.of(
                    "Please review serve out of UK in the scanned form",
                    "Please review respondent by post and applicant will serve application in the scanned form",
                    "Please review respondent address different to service address in the scanned form"
                )
            );
    }
}
