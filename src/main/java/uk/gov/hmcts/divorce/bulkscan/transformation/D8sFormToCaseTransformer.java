package uk.gov.hmcts.divorce.bulkscan.transformation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.common.exception.InvalidDataException;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;

@Component
@Slf4j
@SuppressWarnings({"PMD.PreserveStackTrace"})
public class D8sFormToCaseTransformer extends BulkScanFormTransformer {

    @Autowired
    private Applicant1Transformer applicant1Transformer;

    @Autowired
    private Applicant2Transformer applicant2Transformer;

    @Autowired
    private ApplicationTransformer applicationTransformer;

    @Autowired
    private D8SPrayerTransformer d8SPrayerTransformer;

    @Autowired
    private CommonFormToCaseTransformer commonFormToCaseTransformer;

    @Autowired
    private MarriageDetailsTransformer marriageDetailsTransformer;

    @Autowired
    private PaperFormDetailsTransformer paperFormDetailsTransformer;

    @Override
    protected Map<String, Object> runFormSpecificTransformation(
        List<OcrDataField> ocrDataFieldList,
        boolean automatedProcessCreation,
        String envelopeId
    ) {
        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataFieldList);

        var caseData = CaseData.builder().build();

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

            var transformationDetails =
                TransformationDetails
                    .builder()
                    .caseData(caseData)
                    .ocrDataFields(ocrDataFields)
                    .build();

            List<String> transformationWarnings = transformationDetails.getTransformationWarnings();
            caseData.setDivorceOrDissolution(DIVORCE);
            caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
            caseData.setApplicationType(commonFormToCaseTransformer.getApplicationType(ocrDataFields, transformationWarnings));

            applicant1Transformer
                .andThen(applicant2Transformer)
                .andThen(applicationTransformer)
                .andThen(d8SPrayerTransformer)
                .andThen(marriageDetailsTransformer)
                .andThen(paperFormDetailsTransformer)
                .apply(transformationDetails);

            caseData = commonFormToCaseTransformer.setDefaultValues(caseData);
            transformationWarnings = commonFormToCaseTransformer.verifyFields(transformationDetails, transformationWarnings);

            return commonFormToCaseTransformer.transformCaseData(caseData, transformationWarnings);

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
