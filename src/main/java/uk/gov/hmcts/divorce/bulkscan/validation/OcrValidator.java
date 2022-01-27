package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.endpoint.data.D8Data.transformData;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.getValidationStatus;

@Component
@Slf4j
public class OcrValidator {

    public static final String FIELD_EMPTY_OR_MISSING = "Mandatory field \"%s\" is missing";
    public static final String WARNING_NOT_APPLYING_FINANCIAL_ORDER = "Field must be empty as not applying for financial order: %s";

    public OcrValidationResponse validateExceptionRecord(String formType, OcrDataValidationRequest ocrDataValidationRequest) {
        if (D8.getName().equals(formType)) {
            return validateD8FormType(ocrDataValidationRequest);
        }

        if (D8S.getName().equals(formType)) {
            return validateD8SFormType(ocrDataValidationRequest);
        }

        return OcrValidationResponse.builder()
            .errors(singletonList("Form type '" + formType + "' is invalid"))
            .warnings(emptyList())
            .status(ERRORS)
            .build();
    }

    public OcrValidationResponse validateD8SFormType(OcrDataValidationRequest ocrDataValidationRequest) {

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        return OcrValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .status(getValidationStatus(errors, warnings))
            .build();
    }

    public OcrValidationResponse validateD8FormType(OcrDataValidationRequest ocrDataValidationRequest) {

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, String> data = transformData(ocrDataValidationRequest.getOcrDataFields());

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

    private void validateYourApplication(Map<String, String> data, List<String> warnings, List<String> errors) {

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

    private void validateAboutYou(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1FirstName",data.get("soleOrApplicant1FirstName"));
        validateFields.put("soleApplicantOrApplicant1LastName",data.get("soleApplicantOrApplicant1LastName"));

        if (!isEmpty(data.get("soleOrApplicant1Solicitor"))
            && data.get("soleOrApplicant1Solicitor").equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1SolicitorName",data.get("soleOrApplicant1SolicitorName"));
            validateFields.put("soleOrApplicant1SolicitorFirm",data.get("soleOrApplicant1SolicitorFirm"));
            validateFields.put("soleOrApplicant1SolicitorBuildingAndStreet", data.get("soleOrApplicant1SolicitorBuildingAndStreet"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateAboutTheRespondent(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("respondentOrApplicant2FirstName",data.get("respondentOrApplicant2FirstName"));
        validateFields.put("respondentOrApplicant2LastName",data.get("respondentOrApplicant2LastName"));
        validateFields.put("respondentOrApplicant2MarriedName",data.get("respondentOrApplicant2MarriedName"));

        if (!isEmpty(data.get("respondentOrApplicant2MarriedName"))
            && data.get("respondentOrApplicant2MarriedName").equalsIgnoreCase("yes")
        ) {
            validateFields.put("respondentOrApplicant2WhyMarriedNameChanged", data.get("respondentOrApplicant2WhyMarriedNameChanged"));
        }

        if (!isEmpty(data.get("respondentOrApplicant2Email"))) {
            validateFields.put("respondentEmailAccess",data.get("respondentEmailAccess"));
        }

        if (!isEmpty(data.get("aSoleApplication"))
            && data.get("aSoleApplication").equalsIgnoreCase("true")
        ) {
            validateFields.put("serveOutOfUK",data.get("serveOutOfUK"));

            if (isEmpty(data.get("respondentServePostOnly")) && isEmpty(data.get("applicantWillServeApplication"))) {
                validateFields.put("respondentServePostOnly",data.get("respondentServePostOnly"));
                validateFields.put("applicantWillServeApplication",data.get("applicantWillServeApplication"));
            }

            validateFields.put("respondentDifferentServiceAddress",data.get("respondentDifferentServiceAddress"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateDetailsOfUnion(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        validateFields.put("marriageOutsideOfUK",data.get("marriageOutsideOfUK"));
        if (!isEmpty(data.get("makingAnApplicationWithoutCertificate"))
            && data.get("makingAnApplicationWithoutCertificate").equalsIgnoreCase("yes")
        ) {
            warnings.add("Additional D11 application should be filed with additional fee when applying without certificate");
        }

        if (!isEmpty(data.get("marriageOutsideOfUK")) && !isEmpty(data.get("makingAnApplicationWithoutCertificate"))
            && data.get("marriageOutsideOfUK").equalsIgnoreCase("yes")
            && data.get("makingAnApplicationWithoutCertificate").equalsIgnoreCase("yes")
        ) {
            validateFields.put("placeOfMarriageOrCivilPartnership",data.get("placeOfMarriageOrCivilPartnership"));
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
            validateFields.put("dateOfMarriageOrCivilPartnershipDay",data.get("dateOfMarriageOrCivilPartnershipDay"));
            validateFields.put("dateOfMarriageOrCivilPartnershipMonth",data.get("dateOfMarriageOrCivilPartnershipMonth"));
            validateFields.put("dateOfMarriageOrCivilPartnershipYear",data.get("dateOfMarriageOrCivilPartnershipYear"));
        }

        validateFields.put("soleOrApplicant1FullNameAsOnCert",data.get("soleOrApplicant1FullNameAsOnCert"));
        validateFields.put("respondentOrApplicant2FullNameAsOnCert",data.get("respondentOrApplicant2FullNameAsOnCert"));
        validateFields.put("detailsOnCertCorrect",data.get("detailsOnCertCorrect"));

        if (!isEmpty(data.get("detailsOnCertCorrect"))
            && data.get("detailsOnCertCorrect").equalsIgnoreCase("no")
        ) {
            validateFields.put("reasonWhyCertNotCorrect",data.get("reasonWhyCertNotCorrect"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateJurisdiction(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("jurisdictionReasonsBothPartiesHabitual",data.get("jurisdictionReasonsBothPartiesHabitual"));
        validateFields.put("jurisdictionReasonsBothPartiesLastHabitual",data.get("jurisdictionReasonsBothPartiesLastHabitual"));
        validateFields.put("jurisdictionReasonsRespHabitual",data.get("jurisdictionReasonsRespHabitual"));
        validateFields.put("jurisdictionReasonsJointHabitual",data.get("jurisdictionReasonsJointHabitual"));
        validateFields.put("jurisdictionReasonsJointHabitualWho",data.get("jurisdictionReasonsJointHabitualWho"));
        validateFields.put("jurisdictionReasons1YrHabitual",data.get("jurisdictionReasons1YrHabitual"));
        validateFields.put("jurisdictionReasons6MonthsHabitual",data.get("jurisdictionReasons6MonthsHabitual"));
        validateFields.put("jurisdictionReasonsBothPartiesDomiciled",data.get("jurisdictionReasonsBothPartiesDomiciled"));
        validateFields.put("jurisdictionReasonsOnePartyDomiciled",data.get("jurisdictionReasonsOnePartyDomiciled"));

        if (validateFields.entrySet().stream().allMatch(e -> isEmpty(e.getValue()))
            && isEmpty(data.get("jurisdictionReasonsSameSex"))
        ) {
            warnings.add("Invalid jurisdiction: jurisdiction connection has not been selected");
        }
    }

    private void validateStatementOfIrretrievableBreakdown(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1ConfirmationOfBreakdown",data.get("soleOrApplicant1ConfirmationOfBreakdown"));
        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));

        if (!isEmpty(data.get("aSoleApplication"))
            && data.get("aSoleApplication").equalsIgnoreCase("true")
            && !isEmpty(data.get("applicant2ConfirmationOfBreakdown"))
        ) {
            warnings.add("applicant2ConfirmationOfBreakdown should not be populated for sole applications");
        } else if (!isEmpty(data.get("aJointApplication"))
            && data.get("aJointApplication").equalsIgnoreCase("true")
            && isEmpty(data.get("applicant2ConfirmationOfBreakdown"))
        ) {
            warnings.add("applicant2ConfirmationOfBreakdown should be populated for joint applications");
        }
    }

    private void validateExistingCourtCases(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        if (!isEmpty(data.get("existingOrPreviousCourtCases"))
            && data.get("existingOrPreviousCourtCases").equalsIgnoreCase("yes")
        ) {
            validateFields.put("existingOrPreviousCourtCaseNumbers",data.get("existingOrPreviousCourtCaseNumbers"));
            validateFields.put("summaryOfExistingOrPreviousCourtCases",data.get("summaryOfExistingOrPreviousCourtCases"));

            validateFields.entrySet().stream()
                .filter(e -> isEmpty(e.getValue()))
                .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
        }
    }

    private void validateMoneyPropertyAndPrayer(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        if (!isEmpty(data.get("soleOrApplicant1FinancialOrder"))
            && data.get("soleOrApplicant1FinancialOrder").equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1FinancialOrderFor",data.get("soleOrApplicant1FinancialOrderFor"));
        }

        if (!isEmpty(data.get("applicant2FinancialOrder"))
            && data.get("applicant2FinancialOrder").equalsIgnoreCase("yes")
        ) {
            validateFields.put("applicant2FinancialOrderFor",data.get("applicant2FinancialOrderFor"));
        }

        if (isEmpty(data.get("prayerMarriageDissolved")) && isEmpty(data.get("prayerCivilPartnershipDissolved"))) {
            warnings.add("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
        }

        if (!isEmpty(data.get("soleOrApplicant1FinancialOrder"))
            && data.get("soleOrApplicant1FinancialOrder").equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1prayerFinancialOrder",data.get("soleOrApplicant1prayerFinancialOrder"));
            validateFields.put("soleOrApplicant1prayerFinancialOrderFor",data.get("soleOrApplicant1prayerFinancialOrderFor"));
            validateFields.put("applicant2PrayerFinancialOrder",data.get("applicant2PrayerFinancialOrder"));
            validateFields.put("applicant2PrayerFinancialOrderFor",data.get("applicant2PrayerFinancialOrderFor"));
        } else if (!isEmpty(data.get("soleOrApplicant1FinancialOrder"))
            && data.get("soleOrApplicant1FinancialOrder").equalsIgnoreCase("no")
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

    private void validateSoT(Map<String, String> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleApplicantOrApplicant1StatementOfTruth",data.get("soleApplicantOrApplicant1StatementOfTruth"));
        validateFields.put("soleApplicantOrApplicant1LegalRepStatementOfTruth",
            data.get("soleApplicantOrApplicant1LegalRepStatementOfTruth"));
        validateFields.put("soleApplicantOrApplicant1OrLegalRepSignature", data.get("soleApplicantOrApplicant1OrLegalRepSignature"));
        validateFields.put("soleApplicantOrApplicant1Signing",data.get("soleApplicantOrApplicant1Signing"));
        validateFields.put("legalRepSigning",data.get("legalRepSigning"));
        validateFields.put("statementOfTruthDateDay",data.get("statementOfTruthDateDay"));
        validateFields.put("statementOfTruthDateMonth",data.get("statementOfTruthDateMonth"));
        validateFields.put("statementOfTruthDateYear",data.get("statementOfTruthDateYear"));
        validateFields.put("soleApplicantOrApplicant1OrLegalRepFullName",data.get("soleApplicantOrApplicant1OrLegalRepFullName"));

        if (!isEmpty(data.get("aJointApplication"))
            && data.get("aJointApplication").equalsIgnoreCase("true")
        ) {
            validateFields.put("applicant2StatementOfTruth",data.get("applicant2StatementOfTruth"));
            validateFields.put("applicant2LegalRepStatementOfTruth",data.get("applicant2LegalRepStatementOfTruth"));
            validateFields.put("applicant2OrLegalRepSignature",data.get("applicant2OrLegalRepSignature"));
            validateFields.put("applicant2Signing",data.get("applicant2Signing"));
            validateFields.put("applicant2LegalRepSigning",data.get("applicant2LegalRepSigning"));
            validateFields.put("applicant2StatementOfTruthDateDay",data.get("applicant2StatementOfTruthDateDay"));
            validateFields.put("applicant2StatementOfTruthDateMonth",data.get("applicant2StatementOfTruthDateMonth"));
            validateFields.put("applicant2StatementOfTruthDateYear", data.get("applicant2StatementOfTruthDateYear"));
            validateFields.put("applicant2OrLegalRepFullName", data.get("applicant2OrLegalRepFullName"));
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));

        if (!isEmpty(data.get("soleOrApplicant1HWFNo"))
            && data.get("soleOrApplicant1HWFNo").length() != 6
        ) {
            warnings.add("soleOrApplicant1HWFNo should be 6 digits long");
        }
    }
}
