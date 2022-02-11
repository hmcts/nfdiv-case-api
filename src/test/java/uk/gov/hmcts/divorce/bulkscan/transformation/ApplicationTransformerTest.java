package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.time.Clock;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;

@ExtendWith(MockitoExtension.class)
public class ApplicationTransformerTest {
    @InjectMocks
    private ApplicationTransformer applicationTransformer;

    @Mock
    private Clock clock;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @BeforeEach
    void setUp() {
        setMockClock(clock);
    }

    @Test
    void shouldSuccessfullyTransformApplicationWithoutWarnings() throws Exception {

        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings()).isEmpty();

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/application-transformed.json", Application.class);

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringFields("dateSubmitted")
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);

        assertThat(transformedOutput.getCaseData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSuccessfullyTransformApplicationWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidOcrJson = loadJson("src/test/resources/transformation/input/invalid-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getTransformationAndOcrWarnings())
            .containsExactlyInAnyOrder(
                "Please verify jurisdiction connections(invalid domiciled who) in scanned form",
                "Please verify jurisdiction connections(no options selected) in scanned form",
                "Please review prayer in the scanned form",
                "Please review HWF number for applicant1 in scanned form",
                "Please review HWF number for applicant2 in scanned form"
            );

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/application-transformed-warnings.json", Application.class);

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringFields("dateSubmitted")
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);

        assertThat(transformedOutput.getCaseData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }
}
