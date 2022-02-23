package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8S;

@Component
@Slf4j
public class D8sFormToCaseTransformer extends BulkScanFormTransformer {

    @Autowired
    private OcrValidator validator;

    @Autowired
    private Applicant1Transformer applicant1Transformer;

    @Autowired
    private Applicant2Transformer applicant2Transformer;

    @Autowired
    private ApplicationTransformer applicationTransformer;

    @Autowired
    private D8SPrayerTransformer d8SPrayerTransformer;

    @Autowired
    private CommonTransformer commonTransformer;

    @Autowired
    private MarriageDetailsTransformer marriageDetailsTransformer;

    @Autowired
    private PaperFormDetailsTransformer paperFormDetailsTransformer;

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFieldList) {
        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataFieldList);

        OcrValidationResponse ocrValidationResponse = validator.validateOcrData(D8S.getName(), ocrDataFields);

        if (!isEmpty(ocrValidationResponse.getErrors())) {
            throw new InvalidDataException(
                "OCR validation errors",
                ocrValidationResponse.getWarnings(),
                ocrValidationResponse.getErrors()
            );
        }
        var caseData = CaseData.builder().build();

        List<String> transformationWarnings = caseData.getTransformationAndOcrWarnings();

        try {
            /*
             Section 1 – Your application
             Section 2 – About you(the sole applicant or applicant 1)
             Section 3 – About the respondent or applicant 2
             Section 4 – Details of marriage/civil partnership
             Section 5 – Why this court can deal with your case (Jurisdiction)
             Section 6 – Statement of irretrievable breakdown (the legal reason for your divorce or dissolution)
             Section 7 – Existing or previous court cases
             Section 8 – Dividing your money and property – Orders which are sought
             Section 9 – Summary of what is being applied for (the prayer)
             Section 10 – Statement of truth
             Court fee
             Set label content
             Set gender
             Set application submitted date
             */
            caseData.setDivorceOrDissolution(DIVORCE);
            caseData.setApplicationType(commonTransformer.getApplicationType(ocrDataFields, transformationWarnings));

            var transformationDetails =
                TransformationDetails
                    .builder()
                    .caseData(caseData)
                    .ocrDataFields(ocrDataFields)
                    .build();

            applicant1Transformer
                .andThen(applicant2Transformer)
                .andThen(applicationTransformer)
                .andThen(d8SPrayerTransformer)
                .andThen(marriageDetailsTransformer)
                .andThen(paperFormDetailsTransformer)
                .apply(transformationDetails);

            caseData = commonTransformer.setLabelContentAndDefaultValues(caseData);
            transformationWarnings = commonTransformer.verifyFields(transformationDetails, transformationWarnings);

            return commonTransformer.transformCaseData(caseData, transformationWarnings, ocrValidationResponse);

        } catch (Exception exception) {
            //this will result in bulk scan service to create exception record if case creation is automatic case creation
            // In case of caseworker triggering the event it will result into error shown on the UI
            log.error("Exception occurred while transforming D8S form with error", exception);
            throw new InvalidDataException(
                exception.getMessage(),
                null,
                singletonList("Some error occurred during D8S Form transformation.")
            );
        }
    }
}
