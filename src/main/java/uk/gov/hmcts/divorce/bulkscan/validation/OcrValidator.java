package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.data.KeyValue;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.endpoint.data.ValidationStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.WARNINGS;

@Component
@Slf4j
public class OcrValidator {


    public OcrValidationResponse validateExceptionRecord(OcrDataValidationRequest ocrDataValidationRequest) {

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Object> data = transformData(ocrDataValidationRequest.getOcrDataFields());


        return OcrValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .status(getValidationStatus(errors, warnings))
            .build();
    }

    private void validateYourApplication(Map<String, Object> data) {
        // Section Your Application

        // applicationForDivorce
        // applicationForDissolution
        // aSoleApplication
        // aJointApplication
        // marriageOrCivilPartnershipCertificate
        // translation
        // If field is empty validation status = warning
        // If field is not empty, status = success
    }

    private void validateAboutYou(Map<String, Object> data) {
        // Section About You


        // If field is empty validation status = warning
        // If field is not empty, status = success
    }

    private void validateAboutTheRespondent(Map<String, Object> data) {
        // Section About The Respondent


//        if (joint application) {
//
//        }

        // If field is empty validation status = warning
        // If field is not empty, status = success
    }

    private void validateDetailsOfUnion(Map<String, Object> data) {

    }

    private void validateJurisdiction(Map<String, Object> data) {

    }

    private void validateStatementOfIrretrievableBreakdown(Map<String, Object> data) {

    }

    private void validateExistingCourtCases(Map<String, Object> data) {

    }

    private void validateMoneyAndProperty(Map<String, Object> data) {

    }

    private void validatePrayer(Map<String, Object> data) {

    }

    private void validateSoT(Map<String, Object> data) {

    }


    private static ValidationStatus getValidationStatus(List<String> errors, List<String> warnings) {
        if (!ObjectUtils.isEmpty(errors)) {
            return ERRORS;
        }
        if (!ObjectUtils.isEmpty(warnings)) {
            return WARNINGS;
        }
        return ValidationStatus.SUCCESS;
    }

    private Map<String, Object> transformData(List<KeyValue> ocrDataFields) {
        return ocrDataFields.stream()
            .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
    }
}
