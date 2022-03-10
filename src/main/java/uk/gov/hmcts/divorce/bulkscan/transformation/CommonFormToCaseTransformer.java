package uk.gov.hmcts.divorce.bulkscan.transformation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;

@Component
@Slf4j
public class CommonFormToCaseTransformer {

    public static final String OCR_FIELD_VALUE_BOTH = "both";
    public static final String TRANSFORMATION_AND_OCR_WARNINGS = "warnings";
    public static final String OCR_FIELD_VALUE_YES = "yes";
    public static final String OCR_FIELD_VALUE_NO = "no";

    @Autowired
    private ObjectMapper mapper;

    public List<String> verifyFields(TransformationDetails transformationDetails, List<String> transformationWarnings) {

        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        verifyRespondentEmailAccess(ocrDataFields, transformationWarnings);
        verifyServeOutOfUK(caseData.getApplicationType(), ocrDataFields, transformationWarnings);
        verifyHowApplicationIsServed(caseData.getApplicationType(), ocrDataFields, transformationWarnings);

        return transformationWarnings;
    }

    public CaseData setDefaultValues(CaseData caseData) {

        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsSubmitted(NO);
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsDrafted(NO);
        if (!caseData.getApplicationType().isSole()) {
            caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsSubmitted(NO);
            caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsDrafted(NO);
        }

        return caseData;
    }

    public ApplicationType getApplicationType(OcrDataFields ocrDataFields, List<String> warnings) {
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

    public Map<String, Object> transformCaseData(CaseData caseData, List<String> transformationWarnings) {

        Map<String, Object> transformedCaseData = mapper.convertValue(caseData, new TypeReference<>() {
        });

        List<ListValue<String>> warnings = new ArrayList<>();

        if (!isEmpty(transformationWarnings)) {
            transformationWarnings.forEach(
                warning -> {
                    var listValueWarning = ListValue.<String>builder().id(UUID.randomUUID().toString()).value(warning).build();
                    warnings.add(listValueWarning);
                }
            );
        }

        transformedCaseData.put(TRANSFORMATION_AND_OCR_WARNINGS, warnings);

        return transformedCaseData;
    }

    private void verifyHowApplicationIsServed(ApplicationType applicationType, OcrDataFields ocrDataFields, List<String> warnings) {
        if (SOLE_APPLICATION.equals(applicationType)) {
            if (StringUtils.isEmpty(ocrDataFields.getRespondentServePostOnly())
                || StringUtils.isEmpty(ocrDataFields.getApplicantWillServeApplication())) {
                warnings.add("Please review respondent by post and applicant will serve application in the scanned form");
            }
            if (StringUtils.isEmpty(ocrDataFields.getRespondentDifferentServiceAddress())) {
                warnings.add("Please review respondent address different to service address in the scanned form");
            }
        }
    }

    private void verifyServeOutOfUK(ApplicationType applicationType, OcrDataFields ocrDataFields, List<String> warnings) {
        if (SOLE_APPLICATION.equals(applicationType)
            && (OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(ocrDataFields.getServeOutOfUK())
            || StringUtils.isEmpty(ocrDataFields.getServeOutOfUK()))) {
            warnings.add("Please review serve out of UK in the scanned form");
        }
    }

    private void verifyRespondentEmailAccess(OcrDataFields ocrDataFields, List<String> warnings) {
        if (isNotEmpty(ocrDataFields.getRespondentOrApplicant2Email()) && StringUtils.isEmpty(ocrDataFields.getRespondentEmailAccess())) {
            warnings.add("Please verify respondent email access in scanned form");
        }
    }
}
