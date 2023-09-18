package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.ValidationStatus.getValidationStatus;
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformData;

@Component
@Slf4j
public class OcrValidator {

    public static final String FIELD_EMPTY_OR_MISSING_ERROR = "Mandatory field '%s' should not be empty";
    public static final String FIELD_EMPTY_OR_MISSING_WARNING = "Mandatory field '%s' is missing";
    public static final String WARNING_NOT_APPLYING_FINANCIAL_ORDER = "Field must be empty as not applying for financial order: %s";
    public static final String FIELDS_CANNOT_MATCH = "Fields cannot have the same value: %s, %s";

    public OcrValidationResponse validateExceptionRecord(String formType, OcrDataValidationRequest ocrDataValidationRequest) {
        if (D8.getName().equals(formType) || D8S.getName().equals(formType)) {
            OcrDataFields ocrDataFields = transformData(ocrDataValidationRequest == null ? null
                : ocrDataValidationRequest.getOcrDataFields());
            return validateOcrData(formType, ocrDataFields);
        }

        return OcrValidationResponse.builder()
            .errors(singletonList("Form type '" + formType + "' is invalid"))
            .warnings(emptyList())
            .status(ERRORS)
            .build();
    }

    public OcrValidationResponse validateOcrData(String formType, OcrDataFields data) {

        List<String> warnings = new ArrayList<>();

        validateYourApplication(formType, data, warnings);
        validateAboutYou(data, warnings);
        validateAboutTheRespondent(data, warnings);
        validateDetailsOfUnion(data, warnings);
        validateJurisdiction(data, warnings);
        validateStatementOfIrretrievableBreakdown(data, warnings);
        validateExistingCourtCases(data, warnings);
        validateMoneyProperty(formType, data, warnings);
        validatePrayer(formType, data, warnings);
        validateSoT(data, warnings);

        List<String> errors = emptyList();

        return OcrValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .status(getValidationStatus(errors, warnings))
            .build();
    }

    private void validateYourApplication(String formType, OcrDataFields data, List<String> warnings) {

        if (D8.getName().equals(formType)
            && isEmpty(data.getApplicationForDivorce())
            && isEmpty(data.getApplicationForDissolution())
        ) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicationForDivorce"));
        }
        if (isEmpty(data.getSoleApplication()) && isEmpty(data.getJointApplication())) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "aSoleApplication"));
        } else if (Objects.equals(data.getSoleApplication(), data.getJointApplication())) {
            warnings.add(String.format(FIELDS_CANNOT_MATCH, "aSoleApplication", "aJointApplication"));
        }
        if (isEmpty(data.getMarriageOrCivilPartnershipCertificate())) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "marriageOrCivilPartnershipCertificate"));
        }

        if (Objects.equals(data.getMarriageOrCivilPartnershipCertificate(), data.getTranslation())) {
            warnings.add(String.format(FIELDS_CANNOT_MATCH, "marriageOrCivilPartnershipCertificate", "translation"));
        }
    }

    private void validateAboutYou(OcrDataFields data, List<String> warnings) {

        Map<String, String> validateWarningFields = new HashMap<>();
        Map<String, String> validateErrorFields = new HashMap<>();

        validateErrorFields.put("soleApplicantOrApplicant1FirstName", data.getSoleApplicantOrApplicant1FirstName());
        validateErrorFields.put("soleApplicantOrApplicant1LastName", data.getSoleApplicantOrApplicant1LastName());

        if (!isEmpty(data.getSoleOrApplicant1Solicitor())
            && data.getSoleOrApplicant1Solicitor().equalsIgnoreCase("yes")
        ) {
            validateErrorFields.put("soleOrApplicant1SolicitorName", data.getSoleOrApplicant1SolicitorName());
            validateWarningFields.put("soleOrApplicant1SolicitorFirm", data.getSoleOrApplicant1SolicitorFirm());
            validateWarningFields.put("soleOrApplicant1SolicitorBuildingAndStreet", data.getSoleOrApplicant1SolicitorBuildingAndStreet());
        }

        validateWarningFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_WARNING, e.getKey())));

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));
    }

    private void validateAboutTheRespondent(OcrDataFields data, List<String> warnings) {

        Map<String, String> validateWarningFields = new HashMap<>();
        Map<String, String> validateErrorFields = new HashMap<>();

        validateErrorFields.put("respondentOrApplicant2FirstName", data.getRespondentOrApplicant2FirstName());
        validateErrorFields.put("respondentOrApplicant2LastName", data.getRespondentOrApplicant2LastName());
        validateErrorFields.put("respondentOrApplicant2MarriedName", data.getRespondentOrApplicant2MarriedName());

        if (!isEmpty(data.getRespondentOrApplicant2MarriedName())
            && data.getRespondentOrApplicant2MarriedName().equalsIgnoreCase("yes")
        ) {
            validateErrorFields.put("respondentOrApplicant2WhyMarriedNameChanged", data.getRespondentOrApplicant2WhyMarriedNameChanged());
        }

        if (!isEmpty(data.getRespondentOrApplicant2Email())) {
            validateWarningFields.put("respondentEmailAccess", data.getRespondentEmailAccess());
        }

        if (!isEmpty(data.getSoleApplication())
            && data.getSoleApplication().equalsIgnoreCase("true")
        ) {
            validateWarningFields.put("serveOutOfUK", data.getServeOutOfUK());

            if (isEmpty(data.getRespondentServePostOnly()) && isEmpty(data.getApplicantWillServeApplication())) {
                validateWarningFields.put("respondentServePostOnly", data.getRespondentServePostOnly());
                validateWarningFields.put("applicantWillServeApplication", data.getApplicantWillServeApplication());
            }

            validateWarningFields.put("respondentDifferentServiceAddress", data.getRespondentDifferentServiceAddress());
        }

        validateWarningFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_WARNING, e.getKey())));

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));
    }

    private void validateDetailsOfUnion(OcrDataFields data, List<String> warnings) {

        Map<String, String> validateErrorFields = new HashMap<>();

        validateErrorFields.put("marriageOutsideOfUK", data.getMarriageOutsideOfUK());
        if (!isEmpty(data.getMakingAnApplicationWithoutCertificate())
            && data.getMakingAnApplicationWithoutCertificate().equalsIgnoreCase("yes")
        ) {
            warnings.add("Additional D11 application should be filed with additional fee when applying without certificate");
        }

        if (!isEmpty(data.getMarriageOutsideOfUK()) && !isEmpty(data.getMakingAnApplicationWithoutCertificate())
            && data.getMarriageOutsideOfUK().equalsIgnoreCase("yes")
            && data.getMakingAnApplicationWithoutCertificate().equalsIgnoreCase("yes")
        ) {
            validateErrorFields.put("placeOfMarriageOrCivilPartnership", data.getPlaceOfMarriageOrCivilPartnership());
        }

        if (!isEmpty(data.getDateOfMarriageOrCivilPartnershipDay())
            && !isEmpty(data.getDateOfMarriageOrCivilPartnershipMonth())
            && !isEmpty(data.getDateOfMarriageOrCivilPartnershipYear())
        ) {
            String date = String.format("%s/%s/%s",
                data.getDateOfMarriageOrCivilPartnershipDay(),
                data.getDateOfMarriageOrCivilPartnershipMonth(),
                data.getDateOfMarriageOrCivilPartnershipYear()
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
            validateErrorFields.put("dateOfMarriageOrCivilPartnershipDay", data.getDateOfMarriageOrCivilPartnershipDay());
            validateErrorFields.put("dateOfMarriageOrCivilPartnershipMonth", data.getDateOfMarriageOrCivilPartnershipMonth());
            validateErrorFields.put("dateOfMarriageOrCivilPartnershipYear", data.getDateOfMarriageOrCivilPartnershipYear());
        }

        validateErrorFields.put("soleOrApplicant1FullNameAsOnCert", data.getSoleOrApplicant1FullNameAsOnCert());
        validateErrorFields.put("respondentOrApplicant2FullNameAsOnCert", data.getRespondentOrApplicant2FullNameAsOnCert());
        validateErrorFields.put("detailsOnCertCorrect", data.getDetailsOnCertCorrect());

        if (!isEmpty(data.getDetailsOnCertCorrect())
            && data.getDetailsOnCertCorrect().equalsIgnoreCase("no")
        ) {
            validateErrorFields.put("reasonWhyCertNotCorrect", data.getReasonWhyCertNotCorrect());
        }

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));
    }

    private void validateJurisdiction(OcrDataFields data, List<String> warnings) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("jurisdictionReasonsBothPartiesHabitual", data.getJurisdictionReasonsBothPartiesHabitual());
        validateFields.put("jurisdictionReasonsBothPartiesLastHabitual", data.getJurisdictionReasonsBothPartiesLastHabitual());
        validateFields.put("jurisdictionReasonsRespHabitual", data.getJurisdictionReasonsRespHabitual());
        validateFields.put("jurisdictionReasonsJointHabitual", data.getJurisdictionReasonsJointHabitual());
        validateFields.put("jurisdictionReasonsJointHabitualWho", data.getJurisdictionReasonsJointHabitualWho());
        validateFields.put("jurisdictionReasons1YrHabitual", data.getJurisdictionReasons1YrHabitual());
        validateFields.put("jurisdictionReasons6MonthsHabitual", data.getJurisdictionReasons6MonthsHabitual());
        validateFields.put("jurisdictionReasonsBothPartiesDomiciled", data.getJurisdictionReasonsBothPartiesDomiciled());
        validateFields.put("jurisdictionReasonsOnePartyDomiciled", data.getJurisdictionReasonsOnePartyDomiciled());

        if (validateFields.entrySet().stream().allMatch(e -> isEmpty(e.getValue()))
            && isEmpty(data.getJurisdictionReasonsSameSex())
        ) {
            warnings.add("Invalid jurisdiction: jurisdiction connection has not been selected");
        }
    }

    private void validateStatementOfIrretrievableBreakdown(OcrDataFields data, List<String> warnings) {

        if (isEmpty(data.getSoleOrApplicant1ConfirmationOfBreakdown())) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1ConfirmationOfBreakdown"));
        }

        if (!isEmpty(data.getSoleApplication())
            && data.getSoleApplication().equalsIgnoreCase("true")
            && !isEmpty(data.getApplicant2ConfirmationOfBreakdown())
        ) {
            warnings.add("applicant2ConfirmationOfBreakdown should not be populated for sole applications");
        } else if (!isEmpty(data.getJointApplication())
            && data.getJointApplication().equalsIgnoreCase("true")
            && isEmpty(data.getApplicant2ConfirmationOfBreakdown())
        ) {
            warnings.add("applicant2ConfirmationOfBreakdown should be populated for joint applications");
        }
    }

    private void validateExistingCourtCases(OcrDataFields data, List<String> warnings) {

        Map<String, String> validateErrorFields = new HashMap<>();

        validateErrorFields.put("existingOrPreviousCourtCases", data.getExistingOrPreviousCourtCases());
        if (!isEmpty(data.getExistingOrPreviousCourtCases())
            && data.getExistingOrPreviousCourtCases().equalsIgnoreCase("yes")
        ) {
            validateErrorFields.put("existingOrPreviousCourtCaseNumbers", data.getExistingOrPreviousCourtCaseNumbers());
            validateErrorFields.put("summaryOfExistingOrPreviousCourtCases", data.getSummaryOfExistingOrPreviousCourtCases());
        }

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));
    }

    private void validateMoneyProperty(String formType, OcrDataFields data, List<String> warnings) {

        Map<String, String> validateErrorFields = new HashMap<>();
        Map<String, String> validateWarningFields = new HashMap<>();

        if (!isEmpty(data.getSoleOrApplicant1FinancialOrder())
            && data.getSoleOrApplicant1FinancialOrder().equalsIgnoreCase("yes")
        ) {
            validateErrorFields.put("soleOrApplicant1FinancialOrderFor", data.getSoleOrApplicant1FinancialOrderFor());

            if (D8.getName().equals(formType)) {
                validateWarningFields.put("soleOrApplicant1PrayerFinancialOrder", data.getSoleOrApplicant1prayerFinancialOrder());
            }
            validateWarningFields.put("soleOrApplicant1PrayerFinancialOrderFor", data.getSoleOrApplicant1prayerFinancialOrderFor());
        } else if (!isEmpty(data.getSoleOrApplicant1FinancialOrder())
            && data.getSoleOrApplicant1FinancialOrder().equalsIgnoreCase("no")
        ) {
            if (D8.getName().equals(formType) && !isEmpty(data.getSoleOrApplicant1prayerFinancialOrder())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrder"));
            }
            if (!isEmpty(data.getSoleOrApplicant1prayerFinancialOrderFor())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrderFor"));
            }
        }

        if (!isEmpty(data.getApplicant2FinancialOrder())
            && data.getApplicant2FinancialOrder().equalsIgnoreCase("yes")
        ) {
            validateErrorFields.put("applicant2FinancialOrderFor", data.getApplicant2FinancialOrderFor());

            if (D8.getName().equals(formType)) {
                validateWarningFields.put("applicant2PrayerFinancialOrder", data.getApplicant2PrayerFinancialOrder());
            }
            validateWarningFields.put("applicant2PrayerFinancialOrderFor", data.getApplicant2PrayerFinancialOrderFor());
        } else if (!isEmpty(data.getApplicant2FinancialOrder())
            && data.getApplicant2FinancialOrder().equalsIgnoreCase("no")
        ) {
            if (D8.getName().equals(formType) && !isEmpty(data.getApplicant2PrayerFinancialOrder())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrder"));
            }
            if (!isEmpty(data.getApplicant2PrayerFinancialOrderFor())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrderFor"));
            }
        }

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));

        validateWarningFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_WARNING, e.getKey())));
    }

    private void validatePrayer(String formType, OcrDataFields data, List<String> warnings) {

        Map<String, String> validateErrorFields = new HashMap<>();

        if (D8.getName().equals(formType)) {
            if (isEmpty(data.getPrayerMarriageDissolved()) && isEmpty(data.getPrayerCivilPartnershipDissolved())) {
                warnings.add("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
            }
        } else {
            validateErrorFields.put("prayerApplicant1JudiciallySeparated", data.getPrayerApplicant1JudiciallySeparated());
        }

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));
    }

    private void validateSoT(OcrDataFields data, List<String> warnings) {

        Map<String, String> validateWarningFields = new HashMap<>();
        Map<String, String> validateErrorFields = new HashMap<>();

        validateErrorFields.put("soleApplicantOrApplicant1StatementOfTruth", data.getSoleApplicantOrApplicant1StatementOfTruth());
        validateErrorFields.put("soleApplicantOrApplicant1LegalRepStatementOfTruth",
            data.getSoleApplicantOrApplicant1LegalRepStatementOfTruth());
        validateWarningFields.put("soleApplicantOrApplicant1OrLegalRepSignature", data.getSoleApplicantOrApplicant1OrLegalRepSignature());
        validateWarningFields.put("soleApplicantOrApplicant1Signing", data.getSoleApplicantOrApplicant1Signing());
        validateWarningFields.put("legalRepSigning", data.getLegalRepSigning());
        validateWarningFields.put("statementOfTruthDateDay", data.getStatementOfTruthDateDay());
        validateWarningFields.put("statementOfTruthDateMonth", data.getStatementOfTruthDateMonth());
        validateWarningFields.put("statementOfTruthDateYear", data.getStatementOfTruthDateYear());
        validateWarningFields.put("soleApplicantOrApplicant1OrLegalRepFullName", data.getSoleApplicantOrApplicant1OrLegalRepFullName());

        if (!isEmpty(data.getJointApplication())
            && data.getJointApplication().equalsIgnoreCase("true")
        ) {
            validateErrorFields.put("applicant2StatementOfTruth", data.getApplicant2StatementOfTruth());
            validateErrorFields.put("applicant2LegalRepStatementOfTruth", data.getApplicant2LegalRepStatementOfTruth());
            validateWarningFields.put("applicant2OrLegalRepSignature", data.getApplicant2OrLegalRepSignature());
            validateWarningFields.put("applicant2Signing", data.getApplicant2Signing());
            validateWarningFields.put("applicant2LegalRepSigning", data.getApplicant2LegalRepSigning());
            validateWarningFields.put("applicant2StatementOfTruthDateDay", data.getApplicant2StatementOfTruthDateDay());
            validateWarningFields.put("applicant2StatementOfTruthDateMonth", data.getApplicant2StatementOfTruthDateMonth());
            validateWarningFields.put("applicant2StatementOfTruthDateYear", data.getApplicant2StatementOfTruthDateYear());
            validateWarningFields.put("applicant2OrLegalRepFullName", data.getApplicant2OrLegalRepFullName());
        }

        validateWarningFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_WARNING, e.getKey())));

        validateErrorFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING_ERROR, e.getKey())));

        if (!isEmpty(data.getSoleOrApplicant1HWFNo())
            && data.getSoleOrApplicant1HWFNo().length() != 6
        ) {
            warnings.add("soleOrApplicant1HWFNo should be 6 digits long");
        }

        if (!isEmpty(data.getApplicant2HWFNo())
            && data.getApplicant2HWFNo().length() != 6
        ) {
            warnings.add("applicant2HWFNo should be 6 digits long");
        }
    }
}
