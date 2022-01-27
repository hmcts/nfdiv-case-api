package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.KeyValue;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.endpoint.data.ValidationStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.WARNINGS;

@Component
@Slf4j
public class OcrValidator {

    public static final String FIELD_EMPTY_OR_MISSING = "Field is empty or missing: %s";
    public static final String WARNING_NOT_APPLYING_FINANCIAL_ORDER = "Field must be empty as not applying for financial order: %s";

    public OcrValidationResponse validateExceptionRecord(OcrDataValidationRequest ocrDataValidationRequest) {

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, Object> data = transformData(ocrDataValidationRequest.getOcrDataFields());

        validateYourApplication(data, warnings, errors);
        validateAboutYou(data, warnings, errors);
        validateAboutTheRespondent(data, warnings, errors);
        validateDetailsOfUnion(data, warnings, errors);
        validateJurisdiction(data, warnings, errors);
        validateStatementOfIrretrievableBreakdown(data, warnings, errors);
        validateExistingCourtCases(data, warnings, errors);
        validateMoneyPropertyAndPrayer(data, warnings, errors);
        validateSoT(data, warnings, errors);

        return OcrValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .status(getValidationStatus(errors, warnings))
            .build();
    }

    private void validateYourApplication(Map<String, Object> data, List<String> warnings, List<String> errors) {

        if (isEmpty(data.get("applicationForDivorce")) && isEmpty(data.get("applicationForDissolution"))) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "applicationForDivorce"));
        }
        if (isEmpty(data.get("aSoleApplication")) && isEmpty(data.get("aJointApplication"))) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "aSoleApplication"));
        }
        if (isEmpty(data.get("marriageOrCivilPartnershipCertificate"))) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "marriageOrCivilPartnershipCertificate"));
        }
        if (isEmpty(data.get("translation"))) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "translation"));
        }
    }

    private void validateAboutYou(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1FirstName", (String) data.get("soleOrApplicant1FirstName"));
        validateFields.put("soleApplicantOrApplicant1LastName", (String) data.get("soleApplicantOrApplicant1LastName"));

        if (!isEmpty(data.get("soleOrApplicant1Solicitor"))
            && ((String) data.get("soleOrApplicant1Solicitor")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1SolicitorName", (String) data.get("soleOrApplicant1SolicitorName"));
            validateFields.put("soleOrApplicant1SolicitorFirm", (String) data.get("soleOrApplicant1SolicitorFirm"));
            validateFields.put("soleOrApplicant1SolicitorBuildingAndStreet",
                (String) data.get("soleOrApplicant1SolicitorBuildingAndStreet"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateAboutTheRespondent(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("respondentOrApplicant2FirstName", (String) data.get("respondentOrApplicant2FirstName"));
        validateFields.put("respondentOrApplicant2LastName", (String) data.get("respondentOrApplicant2LastName"));
        validateFields.put("respondentOrApplicant2MarriedName", (String) data.get("respondentOrApplicant2MarriedName"));

        if (!isEmpty(data.get("respondentOrApplicant2MarriedName"))
            && ((String) data.get("respondentOrApplicant2MarriedName")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("respondentOrApplicant2WhyMarriedNameChanged",
                (String) data.get("respondentOrApplicant2WhyMarriedNameChanged"));
        }

        if (!isEmpty(data.get("respondentOrApplicant2Email"))) {
            validateFields.put("respondentEmailAccess", (String) data.get("respondentEmailAccess"));
        }

        if (!isEmpty(data.get("aSoleApplication"))
            && ((String) data.get("aSoleApplication")).equalsIgnoreCase("true")
        ) {
            validateFields.put("serveOutOfUK", (String) data.get("serveOutOfUK"));

            if (isEmpty(data.get("respondentServePostOnly")) && isEmpty(data.get("applicantWillServeApplication"))) {
                validateFields.put("respondentServePostOnly", (String) data.get("respondentServePostOnly"));
                validateFields.put("applicantWillServeApplication", (String) data.get("applicantWillServeApplication"));
            }

            validateFields.put("respondentDifferentServiceAddress", (String) data.get("respondentDifferentServiceAddress"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateDetailsOfUnion(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        validateFields.put("marriageOutsideOfUK", (String) data.get("marriageOutsideOfUK"));
        if (!isEmpty(data.get("makingAnApplicationWithoutCertificate"))
            && ((String) data.get("makingAnApplicationWithoutCertificate")).equalsIgnoreCase("yes")
        ) {
            warnings.add("Additional D11 application should be filed with additional fee when applying without certificate");
        }

        if (!isEmpty(data.get("marriageOutsideOfUK")) && !isEmpty(data.get("makingAnApplicationWithoutCertificate"))
            && ((String) data.get("marriageOutsideOfUK")).equalsIgnoreCase("yes")
            && ((String) data.get("makingAnApplicationWithoutCertificate")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("placeOfMarriageOrCivilPartnership", (String) data.get("placeOfMarriageOrCivilPartnership"));
        }

        if (!isEmpty(data.get("dateOfMarriageOrCivilPartnershipDay"))
            && !isEmpty(data.get("dateOfMarriageOrCivilPartnershipMonth"))
            && !isEmpty(data.get("dateOfMarriageOrCivilPartnershipYear"))
        ) {
            String date = String.format("%s/%s/%s",
                data.get("dateOfMarriageOrCivilPartnershipDay"),
                data.get("dateOfMarriageOrCivilPartnershipMonth"),
                data.get("dateOfMarriageOrCivilPartnershipYear")
            );
            try {
                LocalDate.parse(
                    date,
                    DateTimeFormatter
                        .ofPattern("dd/MM/uuuu")
                        .withResolverStyle(ResolverStyle.STRICT));
            } catch (DateTimeParseException e) {
                warnings.add("dateOfMarriageOrCivilPartnership is not valid");
            }
        } else {
            validateFields.put("dateOfMarriageOrCivilPartnershipDay", (String) data.get("dateOfMarriageOrCivilPartnershipDay"));
            validateFields.put("dateOfMarriageOrCivilPartnershipMonth", (String) data.get("dateOfMarriageOrCivilPartnershipMonth"));
            validateFields.put("dateOfMarriageOrCivilPartnershipYear", (String) data.get("dateOfMarriageOrCivilPartnershipYear"));
        }

        validateFields.put("soleOrApplicant1FullNameAsOnCert", (String) data.get("soleOrApplicant1FullNameAsOnCert"));
        validateFields.put("respondentOrApplicant2FullNameAsOnCert", (String) data.get("respondentOrApplicant2FullNameAsOnCert"));
        validateFields.put("detailsOnCertCorrect", (String) data.get("detailsOnCertCorrect"));

        if (!isEmpty(data.get("detailsOnCertCorrect"))
            && ((String) data.get("detailsOnCertCorrect")).equalsIgnoreCase("no")
        ) {
            validateFields.put("reasonWhyCertNotCorrect", (String) data.get("reasonWhyCertNotCorrect"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateJurisdiction(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("jurisdictionReasonsBothPartiesHabitual", (String) data.get("jurisdictionReasonsBothPartiesHabitual"));
        validateFields.put("jurisdictionReasonsBothPartiesLastHabitual", (String) data.get("jurisdictionReasonsBothPartiesLastHabitual"));
        validateFields.put("jurisdictionReasonsRespHabitual", (String) data.get("jurisdictionReasonsRespHabitual"));
        validateFields.put("jurisdictionReasonsJointHabitual", (String) data.get("jurisdictionReasonsJointHabitual"));
        validateFields.put("jurisdictionReasonsJointHabitualWho", (String) data.get("jurisdictionReasonsJointHabitualWho"));
        validateFields.put("jurisdictionReasons1YrHabitual", (String) data.get("jurisdictionReasons1YrHabitual"));
        validateFields.put("jurisdictionReasons6MonthsHabitual", (String) data.get("jurisdictionReasons6MonthsHabitual"));
        validateFields.put("jurisdictionReasonsBothPartiesDomiciled", (String) data.get("jurisdictionReasonsBothPartiesDomiciled"));
        validateFields.put("jurisdictionReasonsOnePartyDomiciled", (String) data.get("jurisdictionReasonsOnePartyDomiciled"));

        if (validateFields.entrySet().stream().allMatch(e -> isEmpty(e.getValue()))
            && isEmpty(data.get("jurisdictionReasonsSameSex"))
        ) {
            warnings.add("Invalid jurisdiction: jurisdiction connection has not been selected");
        }
    }

    private void validateStatementOfIrretrievableBreakdown(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1ConfirmationOfBreakdown", (String) data.get("soleOrApplicant1ConfirmationOfBreakdown"));
        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));

        if (!isEmpty(data.get("aSoleApplication"))
            && ((String) data.get("aSoleApplication")).equalsIgnoreCase("true")
            && !isEmpty(data.get("applicant2ConfirmationOfBreakdown"))
        ) {
            warnings.add("applicant2ConfirmationOfBreakdown should not be populated for sole applications");
        } else if (!isEmpty(data.get("aJointApplication"))
            && ((String) data.get("aJointApplication")).equalsIgnoreCase("true")
            && isEmpty(data.get("applicant2ConfirmationOfBreakdown"))
        ) {
            warnings.add("applicant2ConfirmationOfBreakdown should be populated for joint applications");
        }
    }

    private void validateExistingCourtCases(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        if (!isEmpty(data.get("existingOrPreviousCourtCases"))
            && ((String) data.get("existingOrPreviousCourtCases")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("existingOrPreviousCourtCaseNumbers", (String) data.get("existingOrPreviousCourtCaseNumbers"));
            validateFields.put("summaryOfExistingOrPreviousCourtCases", (String) data.get("summaryOfExistingOrPreviousCourtCases"));

            validateFields.entrySet().stream()
                .filter(e -> isEmpty(e.getValue()))
                .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
        }
    }

    private void validateMoneyPropertyAndPrayer(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        if (!isEmpty(data.get("soleOrApplicant1FinancialOrder"))
            && ((String) data.get("soleOrApplicant1FinancialOrder")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1FinancialOrderFor", (String) data.get("soleOrApplicant1FinancialOrderFor"));
        }

        if (!isEmpty(data.get("applicant2FinancialOrder"))
            && ((String) data.get("applicant2FinancialOrder")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("applicant2FinancialOrderFor", (String) data.get("applicant2FinancialOrderFor"));
        }

        if (isEmpty(data.get("prayerMarriageDissolved")) && isEmpty(data.get("prayerCivilPartnershipDissolved"))) {
            warnings.add("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
        }

        if (!isEmpty(data.get("soleOrApplicant1FinancialOrder"))
            && ((String) data.get("soleOrApplicant1FinancialOrder")).equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1prayerFinancialOrder", (String) data.get("soleOrApplicant1prayerFinancialOrder"));
            validateFields.put("soleOrApplicant1prayerFinancialOrderFor", (String) data.get("soleOrApplicant1prayerFinancialOrderFor"));
            validateFields.put("applicant2PrayerFinancialOrder", (String) data.get("applicant2PrayerFinancialOrder"));
            validateFields.put("applicant2PrayerFinancialOrderFor", (String) data.get("applicant2PrayerFinancialOrderFor"));
        } else if (!isEmpty(data.get("soleOrApplicant1FinancialOrder"))
            && ((String) data.get("soleOrApplicant1FinancialOrder")).equalsIgnoreCase("no")
        ) {
            if (!isEmpty(data.get("soleOrApplicant1prayerFinancialOrder"))) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrder"));
            }
            if (!isEmpty(data.get("soleOrApplicant1prayerFinancialOrderFor"))) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrderFor"));
            }
            if (!isEmpty(data.get("applicant2PrayerFinancialOrder"))) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrder"));
            }
            if (!isEmpty(data.get("applicant2PrayerFinancialOrderFor"))) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrderFor"));
            }
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateSoT(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleApplicantOrApplicant1StatementOfTruth", (String) data.get("soleApplicantOrApplicant1StatementOfTruth"));
        validateFields.put("soleApplicantOrApplicant1LegalRepStatementOfTruth",
            (String) data.get("soleApplicantOrApplicant1LegalRepStatementOfTruth"));
        validateFields.put("soleApplicantOrApplicant1OrLegalRepSignature",
            (String) data.get("soleApplicantOrApplicant1OrLegalRepSignature"));
        validateFields.put("soleApplicantOrApplicant1Signing", (String) data.get("soleApplicantOrApplicant1Signing"));
        validateFields.put("legalRepSigning", (String) data.get("legalRepSigning"));
        validateFields.put("statementOfTruthDateDay", (String) data.get("statementOfTruthDateDay"));
        validateFields.put("statementOfTruthDateMonth", (String) data.get("statementOfTruthDateMonth"));
        validateFields.put("statementOfTruthDateYear", (String) data.get("statementOfTruthDateYear"));
        validateFields.put("soleApplicantOrApplicant1OrLegalRepFullName", (String) data.get("soleApplicantOrApplicant1OrLegalRepFullName"));

        if (!isEmpty(data.get("aJointApplication"))
            && ((String) data.get("aJointApplication")).equalsIgnoreCase("true")
        ) {
            validateFields.put("applicant2StatementOfTruth", (String) data.get("applicant2StatementOfTruth"));
            validateFields.put("applicant2LegalRepStatementOfTruth", (String) data.get("applicant2LegalRepStatementOfTruth"));
            validateFields.put("applicant2OrLegalRepSignature", (String) data.get("applicant2OrLegalRepSignature"));
            validateFields.put("applicant2Signing", (String) data.get("applicant2Signing"));
            validateFields.put("applicant2LegalRepSigning", (String) data.get("applicant2LegalRepSigning"));
            validateFields.put("applicant2StatementOfTruthDateDay", (String) data.get("applicant2StatementOfTruthDateDay"));
            validateFields.put("applicant2StatementOfTruthDateMonth", (String) data.get("applicant2StatementOfTruthDateMonth"));
            validateFields.put("applicant2StatementOfTruthDateYear", (String) data.get("applicant2StatementOfTruthDateYear"));
            validateFields.put("applicant2OrLegalRepFullName", (String) data.get("applicant2OrLegalRepFullName"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));

        if (!isEmpty(data.get("soleOrApplicant1HWFNo"))
            && ((String) data.get("soleOrApplicant1HWFNo")).length() != 6
        ) {
            warnings.add("soleOrApplicant1HWFNo should be 6 digits long");
        }
    }


    private static ValidationStatus getValidationStatus(List<String> errors, List<String> warnings) {
        if (!isEmpty(errors)) {
            return ERRORS;
        }
        if (!isEmpty(warnings)) {
            return WARNINGS;
        }
        return ValidationStatus.SUCCESS;
    }

    private Map<String, Object> transformData(List<KeyValue> ocrDataFields) {
        return ocrDataFields.stream()
            .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
    }
}
