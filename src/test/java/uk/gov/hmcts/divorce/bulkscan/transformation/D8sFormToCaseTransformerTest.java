package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.endpoint.model.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.bulkscan.transformation.CommonFormToCaseTransformer.TRANSFORMATION_AND_OCR_WARNINGS;
import static uk.gov.hmcts.divorce.bulkscan.util.FileUtil.loadJson;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.inputScannedDocuments;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.scannedDocuments;

@ExtendWith(MockitoExtension.class)
class D8sFormToCaseTransformerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Captor
    ArgumentCaptor<TransformationDetails> transformationDetailsCaptor;

    @Mock
    private OcrValidator validator;

    @Mock
    private Applicant1Transformer applicant1Transformer;

    @Mock
    private Applicant2Transformer applicant2Transformer;

    @Mock
    private ApplicationTransformer applicationTransformer;

    @Mock
    private D8SPrayerTransformer d8SPrayerTransformer;

    @Mock
    private CommonFormToCaseTransformer commonFormToCaseTransformer;

    @Mock
    private MarriageDetailsTransformer marriageDetailsTransformer;

    @Mock
    private PaperFormDetailsTransformer paperFormDetailsTransformer;

    @InjectMocks
    private D8sFormToCaseTransformer d8sFormToCaseTransformer;

    @Test
    @SuppressWarnings("unchecked")
    void shouldSuccessfullyTransformD8FormWithScannedDocumentsWithoutWarnings() throws Exception {

        final String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8s-form-ocr.json");
        final List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .isJudicialSeparation(YES)
            .build();
        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();
        final OcrValidationResponse ocrValidationResponse = OcrValidationResponse.builder().build();
        final Map<String, Object> expectedResult = emptyMap();

        final Function<TransformationDetails, TransformationDetails> app1App2 = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2Application = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2ApplicationPrayer = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2ApplicationPrayerMarriage = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2ApplicationPrayerMarriagePaper = mock(Function.class);

        when(applicant1Transformer.andThen(applicant2Transformer)).thenReturn(app1App2);
        when(app1App2.andThen(applicationTransformer)).thenReturn(app1App2Application);
        when(app1App2Application.andThen(d8SPrayerTransformer)).thenReturn(app1App2ApplicationPrayer);
        when(app1App2ApplicationPrayer.andThen(marriageDetailsTransformer)).thenReturn(app1App2ApplicationPrayerMarriage);
        when(app1App2ApplicationPrayerMarriage.andThen(paperFormDetailsTransformer)).thenReturn(app1App2ApplicationPrayerMarriagePaper);
        when(app1App2ApplicationPrayerMarriagePaper.apply(any(TransformationDetails.class))).thenReturn(transformationDetails);

        when(validator.validateOcrData(D8S.getName(), transformOcrMapToObject(ocrDataFields)))
            .thenReturn(ocrValidationResponse);
        when(commonFormToCaseTransformer.setDefaultValues(any(CaseData.class)))
            .thenReturn(caseData);
        when(commonFormToCaseTransformer.verifyFields(any(TransformationDetails.class), any(List.class)))
            .thenReturn(emptyList());
        when(commonFormToCaseTransformer.transformCaseData(caseData, emptyList(), ocrValidationResponse))
            .thenReturn(expectedResult);

        final ExceptionRecord exceptionRecord = exceptionRecord(ocrDataFields);
        final var transformedOutput = d8sFormToCaseTransformer.transformIntoCaseData(exceptionRecord);

        verify(app1App2ApplicationPrayerMarriagePaper).apply(transformationDetailsCaptor.capture());

        final TransformationDetails transformationDetailsCaptorValue = transformationDetailsCaptor.getValue();
        final CaseData captorValueCaseData = transformationDetailsCaptorValue.getCaseData();

        assertThat(captorValueCaseData.getDivorceOrDissolution()).isEqualTo(DIVORCE);
        assertThat(captorValueCaseData.getIsJudicialSeparation()).isEqualTo(YES);

        assertThat(transformedOutput.get("scannedDocuments"))
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(scannedDocuments(D8S));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSuccessfullyTransformD8FormWithWarnings() throws Exception {

        final String invalidApplicationOcrJson = loadJson("src/test/resources/transformation/input/invalid-d8s-form-ocr.json");
        final List<OcrDataField> ocrDataFields = MAPPER.readValue(invalidApplicationOcrJson, new TypeReference<>() {
        });

        final var caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .divorceOrDissolution(DIVORCE)
            .build();

        final var transformationDetails =
            TransformationDetails
                .builder()
                .ocrDataFields(transformOcrMapToObject(ocrDataFields))
                .caseData(caseData)
                .build();
        final var ocrValidationResponse = OcrValidationResponse.builder().build();
        final List<ListValue<String>> expectedWarnings = singletonList(ListValue.<String>builder()
            .id(UUID.randomUUID().toString())
            .value("warning")
            .build());

        final Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put(TRANSFORMATION_AND_OCR_WARNINGS, expectedWarnings);

        final Function<TransformationDetails, TransformationDetails> app1App2 = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2Application = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2ApplicationPrayer = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2ApplicationPrayerMarriage = mock(Function.class);
        final Function<TransformationDetails, TransformationDetails> app1App2ApplicationPrayerMarriagePaper = mock(Function.class);

        when(applicant1Transformer.andThen(applicant2Transformer)).thenReturn(app1App2);
        when(app1App2.andThen(applicationTransformer)).thenReturn(app1App2Application);
        when(app1App2Application.andThen(d8SPrayerTransformer)).thenReturn(app1App2ApplicationPrayer);
        when(app1App2ApplicationPrayer.andThen(marriageDetailsTransformer)).thenReturn(app1App2ApplicationPrayerMarriage);
        when(app1App2ApplicationPrayerMarriage.andThen(paperFormDetailsTransformer)).thenReturn(app1App2ApplicationPrayerMarriagePaper);
        when(app1App2ApplicationPrayerMarriagePaper.apply(any(TransformationDetails.class))).thenReturn(transformationDetails);

        when(validator.validateOcrData(D8S.getName(), transformOcrMapToObject(ocrDataFields)))
            .thenReturn(ocrValidationResponse);
        when(commonFormToCaseTransformer.setDefaultValues(any(CaseData.class)))
            .thenReturn(caseData);
        when(commonFormToCaseTransformer.verifyFields(any(TransformationDetails.class), any(List.class)))
            .thenReturn(emptyList());
        when(commonFormToCaseTransformer.transformCaseData(caseData, emptyList(), ocrValidationResponse))
            .thenReturn(expectedResult);

        final var exceptionRecord = exceptionRecord(ocrDataFields);
        final var transformedOutput = d8sFormToCaseTransformer.transformIntoCaseData(exceptionRecord);
        final List<ListValue<String>> warnings = (List<ListValue<String>>) transformedOutput.get(TRANSFORMATION_AND_OCR_WARNINGS);

        verify(app1App2ApplicationPrayerMarriagePaper).apply(transformationDetailsCaptor.capture());

        final TransformationDetails transformationDetailsCaptorValue = transformationDetailsCaptor.getValue();
        final CaseData captorValueCaseData = transformationDetailsCaptorValue.getCaseData();

        assertThat(captorValueCaseData.getDivorceOrDissolution()).isEqualTo(DIVORCE);
        assertThat(captorValueCaseData.getIsJudicialSeparation()).isEqualTo(YES);

        assertThat(warnings)
            .extracting("value")
            .isEqualTo(
                List.of(
                    "warning"
                )
            );
    }

    @Test
    void shouldThrowInvalidDataExceptionWhenOcrValidationContainsErrors() throws Exception {
        final String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8s-form-ocr.json");
        final List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        when(validator.validateOcrData(D8S.getName(), transformOcrMapToObject(ocrDataFields)))
            .thenReturn(OcrValidationResponse.builder().errors(List.of("some error")).build());

        final ExceptionRecord exceptionRecord = exceptionRecord(ocrDataFields);

        assertThatThrownBy(() -> d8sFormToCaseTransformer.transformIntoCaseData(exceptionRecord))
            .isExactlyInstanceOf(InvalidDataException.class)
            .hasMessageContaining("OCR validation errors")
            .extracting("errors")
            .isEqualTo(List.of("some error"));

    }

    @Test
    void shouldThrowInvalidDataExceptionWhenOcrTransformationThrowsException() throws Exception {
        final String validApplicationOcrJson = loadJson("src/test/resources/transformation/input/valid-d8s-form-ocr.json");
        final List<OcrDataField> ocrDataFields = MAPPER.readValue(validApplicationOcrJson, new TypeReference<>() {
        });

        when(validator.validateOcrData(D8S.getName(), transformOcrMapToObject(ocrDataFields)))
            .thenReturn(OcrValidationResponse.builder().build());

        final ExceptionRecord exceptionRecord = exceptionRecord(ocrDataFields);

        doThrow(new RuntimeException("some exception")).when(applicant1Transformer).andThen(applicant2Transformer);

        assertThatThrownBy(() -> d8sFormToCaseTransformer.transformIntoCaseData(exceptionRecord))
            .isExactlyInstanceOf(InvalidDataException.class)
            .hasMessageContaining("some exception")
            .extracting("errors")
            .isEqualTo(List.of("Some error occurred during D8S Form transformation."));
    }

    private ExceptionRecord exceptionRecord(List<OcrDataField> ocrDataFields) {
        return ExceptionRecord
            .builder()
            .formType(D8.getName())
            .ocrDataFields(ocrDataFields)
            .scannedDocuments(inputScannedDocuments(D8S))
            .build();
    }
}
