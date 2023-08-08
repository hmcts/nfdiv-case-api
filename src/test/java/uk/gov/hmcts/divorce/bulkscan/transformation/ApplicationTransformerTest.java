package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.util.List;
import java.util.Set;

import static joptsimple.internal.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.jsonToObject;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_DOMICILED;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_2_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.RESIDUAL_JURISDICTION_CP;
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
    void shouldSuccessfullyTransformSoleApplicationWithoutWarnings() throws Exception {
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

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

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
    void shouldSuccessfullyTransformSoleApplicationWithoutWarningsWhenOcrFieldValuesAreNull() throws Exception {
        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-application-ocr-with-null-values.json");
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

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

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
    void shouldSuccessfullyTransformSoleApplicationWithoutWarningsWhenApp1IsDomiciledAndDissolution() throws Exception {
        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        OcrDataFields dataFields = transformOcrMapToObject(ocrDataFields);
        dataFields.setApplicationForDissolution("true");
        dataFields.setJurisdictionReasonsOnePartyDomiciled("true");
        dataFields.setJurisdictionReasonsOnePartyDomiciledWho("applicant,applicant1");
        dataFields.setJurisdictionReasonsSameSex("true");
        dataFields.setJurisdictionReasonsRespHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesLastHabitual(EMPTY);
        dataFields.setJurisdictionReasons6MonthsHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesDomiciled(EMPTY);
        dataFields.setJurisdictionReasonsJointHabitual(EMPTY);

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(dataFields)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/application-transformed.json", Application.class);
        expectedApplication.getJurisdiction().setConnections(Set.of(APP_1_DOMICILED, RESIDUAL_JURISDICTION_CP));

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringFields("dateSubmitted")
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);

        assertThat(transformedOutput.getCaseData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSuccessfullyTransformSoleApplicationWithoutWarningsWithApplicant1andApplicant2JointConnections() throws Exception {
        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-joint-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        OcrDataFields dataFields = transformOcrMapToObject(ocrDataFields);
        dataFields.setJurisdictionReasonsOnePartyDomiciled("false");
        dataFields.setJurisdictionReasonsSameSex("false");
        dataFields.setJurisdictionReasonsJointHabitualWho("applicant1,applicant2");
        dataFields.setJurisdictionReasonsRespHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesLastHabitual(EMPTY);
        dataFields.setJurisdictionReasons6MonthsHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesDomiciled(EMPTY);
        dataFields.setJurisdictionReasonsJointHabitual(EMPTY);

        final var caseData = CaseData.builder().applicationType(JOINT_APPLICATION).divorceOrDissolution(DIVORCE).build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(dataFields)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/joint-application-transformed.json", Application.class);
        expectedApplication.getJurisdiction().setConnections(Set.of(APP_1_RESIDENT_JOINT, APP_2_RESIDENT_JOINT));

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringFields("dateSubmitted")
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);

        assertThat(transformedOutput.getCaseData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSuccessfullyTransformSoleApplicationWithoutWarningsWhenApp2IsDomiciledAndResident() throws Exception {
        String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        OcrDataFields dataFields = transformOcrMapToObject(ocrDataFields);
        dataFields.setJurisdictionReasonsOnePartyDomiciled("true");
        dataFields.setJurisdictionReasonsOnePartyDomiciledWho("applicant2");
        dataFields.setJurisdictionReasonsSameSex(EMPTY);
        dataFields.setJurisdictionReasonsRespHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesLastHabitual(EMPTY);
        dataFields.setJurisdictionReasons6MonthsHabitual(EMPTY);
        dataFields.setJurisdictionReasonsBothPartiesDomiciled(EMPTY);

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(dataFields)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings()).isEmpty();

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/application-transformed.json", Application.class);
        expectedApplication.getJurisdiction().setConnections(Set.of(APP_2_DOMICILED));

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringFields("dateSubmitted")
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);

        assertThat(transformedOutput.getCaseData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSuccessfullyTransformSoleApplicationWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidOcrJson = loadJson("src/test/resources/transformation/input/invalid-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(SOLE_APPLICATION).divorceOrDissolution(DIVORCE).build();
        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setSoleOrApplicant1ConfirmationOfBreakdown("false");
        ocrData.setApplicant2ConfirmationOfBreakdown("true");

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please verify jurisdiction connections(missing/invalid domiciled who) in scanned form",
                "Please verify jurisdiction connections(no options selected) in scanned form",
                "Please review confirmation of breakdown for sole application in the scanned form",
                "Please review HWF number for applicant1 in scanned form",
                "Please review HWF number for applicant2 in scanned form"
            );

        final var expectedApplication =
            jsonToObject("src/test/resources/transformation/output/application-transformed-warnings.json", Application.class);
        expectedApplication.setApplicant2ScreenHasMarriageBroken(YES);

        assertThat(transformedOutput.getCaseData().getApplication())
            .usingRecursiveComparison()
            .ignoringFields("dateSubmitted")
            .ignoringActualNullFields()
            .isEqualTo(expectedApplication);

        assertThat(transformedOutput.getCaseData().getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSuccessfullyTransformJointDissolutionApplicationWithWarningsWhenOcrContainsInvalidData() throws Exception {
        String invalidOcrJson = loadJson("src/test/resources/transformation/input/invalid-application-ocr.json");
        List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder().applicationType(JOINT_APPLICATION).divorceOrDissolution(DISSOLUTION).build();
        OcrDataFields ocrData = transformOcrMapToObject(ocrDataFields);
        ocrData.setSoleOrApplicant1ConfirmationOfBreakdown("false");
        ocrData.setApplicant2ConfirmationOfBreakdown("false");

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getTransformationWarnings())
            .containsExactlyInAnyOrder(
                "Please verify jurisdiction connections(missing/invalid domiciled who) in scanned form",
                "Please verify jurisdiction connections(no options selected) in scanned form",
                "Please review confirmation of breakdown for joint application in the scanned form",
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

    @Test
    void shouldIgnoreEmptyStringPassedForHwf() {
        final var caseData = CaseData.builder().applicationType(JOINT_APPLICATION).divorceOrDissolution(DISSOLUTION).build();
        OcrDataFields ocrData = new OcrDataFields();
        ocrData.setSoleOrApplicant1HWFNo("");
        ocrData.setApplicant2HWFNo("");

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(ocrData)
                .caseData(caseData)
                .build();

        final var transformedOutput = applicationTransformer.apply(transformationDetails);

        assertThat(transformedOutput.getCaseData().getApplication().getApplicant1HelpWithFees().getReferenceNumber())
            .isEqualTo("");
        assertThat(transformedOutput.getCaseData().getApplication().getApplicant2HelpWithFees().getReferenceNumber())
            .isEqualTo("");
    }
}
