package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.reform.bsp.common.error.InvalidDataException;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.OcrDataField;
import uk.gov.hmcts.reform.bsp.common.service.transformation.BulkScanFormTransformer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformOcrMapToObject;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;

@Component
@Slf4j
@SuppressWarnings({"PMD.PreserveStackTrace"})
public class D8FormToCaseTransformer extends BulkScanFormTransformer {

    public static final String OCR_FIELD_VALUE_BOTH = "both";
    public static final String TRANSFORMATION_AND_OCR_WARNINGS = "transformationAndOcrWarnings";
    public static final String OCR_FIELD_VALUE_YES = "yes";
    public static final String OCR_FIELD_VALUE_NO = "no";

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private OcrValidator validator;

    @Autowired
    private Applicant1Transformer applicant1Transformer;

    @Autowired
    private Applicant2Transformer applicant2Transformer;

    @Autowired
    private ApplicationTransformer applicationTransformer;

    @Autowired
    private MarriageDetailsTransformer marriageDetailsTransformer;

    @Autowired
    private PaperFormDetailsTransformer paperFormDetailsTransformer;

    @Override
    protected Map<String, String> getOcrToCCDMapping() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Object> runFormSpecificTransformation(List<OcrDataField> ocrDataFieldList) {
        OcrDataFields ocrDataFields = transformOcrMapToObject(ocrDataFieldList);

        OcrValidationResponse ocrValidationResponse = validator.validateOcrData(D8.getName(), ocrDataFields);

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
            caseData.setDivorceOrDissolution(getDivorceType(ocrDataFields, transformationWarnings));
            caseData.setApplicationType(getApplicationType(ocrDataFields, transformationWarnings));

            var transformationDetails =
                TransformationDetails
                    .builder()
                    .caseData(caseData)
                    .ocrDataFields(ocrDataFields)
                    .build();

            applicant1Transformer
                .andThen(applicant2Transformer)
                .andThen(applicationTransformer)
                .andThen(marriageDetailsTransformer)
                .andThen(paperFormDetailsTransformer)
                .apply(transformationDetails);

            caseData.getLabelContent().setApplicationType(caseData.getApplicationType());
            caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

            verifyRespondentEmailAccess(ocrDataFields, transformationWarnings);
            verifyServeOutOfUK(caseData.getApplicationType(), ocrDataFields, transformationWarnings);
            verifyHowApplicationIsServed(caseData.getApplicationType(), ocrDataFields, transformationWarnings);

            caseData.getLabelContent().setApplicationType(caseData.getApplicationType());
            caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

            Map<String, Object> transformedCaseData = mapper.convertValue(caseData, new TypeReference<>() {
            });

            List<String> combinedWarnings = isEmpty(ocrValidationResponse.getWarnings())
                ? transformationWarnings
                : union(ocrValidationResponse.getWarnings(), transformationWarnings);

            transformedCaseData.put(TRANSFORMATION_AND_OCR_WARNINGS, combinedWarnings);

            return transformedCaseData;

        } catch (Exception exception) {
            //this will result in bulk scan service to create exception record if case creation is automatic case creation
            // In case of caseworker triggering the event it will result into error/transformationWarnings shown on the UI
            log.error("Exception occurred while transforming D8 form with error", exception);
            throw new InvalidDataException(
                exception.getMessage(),
                isEmpty(ocrValidationResponse.getWarnings())
                    ? transformationWarnings
                    : union(ocrValidationResponse.getWarnings(), transformationWarnings),
                null
            );
        }
    }

    private void verifyHowApplicationIsServed(ApplicationType applicationType, OcrDataFields ocrDataFields, List<String> warnings) {
        if (SOLE_APPLICATION.equals(applicationType)) {
            if (isEmpty(ocrDataFields.getRespondentServePostOnly()) || isEmpty(ocrDataFields.getApplicantWillServeApplication())) {
                warnings.add("Please review respondent by post and applicant will serve application in the scanned form");
            }
            if (isEmpty(ocrDataFields.getRespondentDifferentServiceAddress())) {
                warnings.add("Please review respondent address different to service address in the scanned form");
            }
        }
    }

    private void verifyServeOutOfUK(ApplicationType applicationType, OcrDataFields ocrDataFields, List<String> warnings) {
        if (SOLE_APPLICATION.equals(applicationType)
            && (OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getServeOutOfUK()) || isEmpty(ocrDataFields.getServeOutOfUK()))) {
            warnings.add("Please review serve out of UK in the scanned form");
        }
    }

    private void verifyRespondentEmailAccess(OcrDataFields ocrDataFields, List<String> warnings) {
        if (isNotEmpty(ocrDataFields.getRespondentOrApplicant2Email()) && isEmpty(ocrDataFields.getRespondentEmailAccess())) {
            warnings.add("Please verify respondent email access in scanned form");
        }
    }

    private ApplicationType getApplicationType(OcrDataFields ocrDataFields, List<String> warnings) {
        boolean isSole = toBoolean(ocrDataFields.getSoleApplication());
        boolean isJoint = toBoolean(ocrDataFields.getJointApplication());
        if (isJoint && !isSole) {
            return JOINT_APPLICATION;
        } else if (isSole && !isJoint) {
            return SOLE_APPLICATION;
        } else {
            warnings.add("Please review application type in the scanned form");
            return SOLE_APPLICATION;
        }
    }

    private DivorceOrDissolution getDivorceType(OcrDataFields ocrDataFields, List<String> warnings) {
        boolean isApplicationForDivorce = toBoolean(ocrDataFields.getApplicationForDivorce());
        boolean isApplicationForDissolution = toBoolean(ocrDataFields.getApplicationForDissolution());

        if (isApplicationForDissolution && !isApplicationForDivorce) {
            return DISSOLUTION;
        } else if (isApplicationForDivorce && !isApplicationForDissolution) {
            return DIVORCE;
        } else {
            warnings.add("Please review divorce type in the scanned form");
            return DIVORCE;
        }
    }
}
