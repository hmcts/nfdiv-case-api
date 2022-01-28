package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
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
import static uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields.transformData;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.getValidationStatus;

@Component
@Slf4j
public class OcrValidator {

    public static final String FIELD_EMPTY_OR_MISSING = "Mandatory field '%s' is missing";
    public static final String WARNING_NOT_APPLYING_FINANCIAL_ORDER = "Field must be empty as not applying for financial order: %s";

    public OcrValidationResponse validateExceptionRecord(String formType, OcrDataValidationRequest ocrDataValidationRequest) {
        if (D8.getName().equals(formType) || D8S.getName().equals(formType)) {
            return validateOcrData(formType, ocrDataValidationRequest);
        }

        return OcrValidationResponse.builder()
            .errors(singletonList("Form type '" + formType + "' is invalid"))
            .warnings(emptyList())
            .status(ERRORS)
            .build();
    }

    private OcrValidationResponse validateOcrData(String formType, OcrDataValidationRequest ocrDataValidationRequest) {

        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        OcrDataFields data = transformData(ocrDataValidationRequest.getOcrDataFields());

        validateYourApplication(formType, data, warnings, errors);
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

    private void validateYourApplication(String formType, OcrDataFields data, List<String> warnings, List<String> errors) {

        if (D8.getName().equals(formType)
            && isEmpty(data.getApplicationForDivorce())
            && isEmpty(data.getApplicationForDissolution())
        ) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "applicationForDivorce"));
        }
        if (isEmpty(data.getSoleApplication()) && isEmpty(data.getJointApplication())) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "aSoleApplication"));
        }
        if (isEmpty(data.getMarriageOrCivilPartnershipCertificate())) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "marriageOrCivilPartnershipCertificate"));
        }
        if (isEmpty(data.getTranslation())) {
            warnings.add(String.format(FIELD_EMPTY_OR_MISSING, "translation"));
        }
    }

    private void validateAboutYou(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleApplicantOrApplicant1FirstName", data.getSoleApplicantOrApplicant1FirstName());
        validateFields.put("soleApplicantOrApplicant1LastName", data.getSoleApplicantOrApplicant1LastName());

        if (!isEmpty(data.getSoleOrApplicant1Solicitor())
            && data.getSoleOrApplicant1Solicitor().equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1SolicitorName", data.getSoleOrApplicant1SolicitorName());
            validateFields.put("soleOrApplicant1SolicitorFirm", data.getSoleOrApplicant1SolicitorFirm());
            validateFields.put("soleOrApplicant1SolicitorBuildingAndStreet", data.getSoleOrApplicant1SolicitorBuildingAndStreet());
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateAboutTheRespondent(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("respondentOrApplicant2FirstName", data.getRespondentOrApplicant2FirstName());
        validateFields.put("respondentOrApplicant2LastName", data.getRespondentOrApplicant2LastName());
        validateFields.put("respondentOrApplicant2MarriedName", data.getRespondentOrApplicant2MarriedName());

        if (!isEmpty(data.getRespondentOrApplicant2MarriedName())
            && data.getRespondentOrApplicant2MarriedName().equalsIgnoreCase("yes")
        ) {
            validateFields.put("respondentOrApplicant2WhyMarriedNameChanged", data.getRespondentOrApplicant2WhyMarriedNameChanged());
        }

        if (!isEmpty(data.getRespondentOrApplicant2Email())) {
            validateFields.put("respondentEmailAccess", data.getRespondentEmailAccess());
        }

        if (!isEmpty(data.getSoleApplication())
            && data.getSoleApplication().equalsIgnoreCase("true")
        ) {
            validateFields.put("serveOutOfUK", data.getServeOutOfUK());

            if (isEmpty(data.getRespondentServePostOnly()) && isEmpty(data.getApplicantWillServeApplication())) {
                validateFields.put("respondentServePostOnly", data.getRespondentServePostOnly());
                validateFields.put("applicantWillServeApplication", data.getApplicantWillServeApplication());
            }

            validateFields.put("respondentDifferentServiceAddress", data.getRespondentDifferentServiceAddress());
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateDetailsOfUnion(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        validateFields.put("marriageOutsideOfUK", data.getMarriageOutsideOfUK());
        if (!isEmpty(data.getMakingAnApplicationWithoutCertificate())
            && data.getMakingAnApplicationWithoutCertificate().equalsIgnoreCase("yes")
        ) {
            warnings.add("Additional D11 application should be filed with additional fee when applying without certificate");
        }

        if (!isEmpty(data.getMarriageOutsideOfUK()) && !isEmpty(data.getMakingAnApplicationWithoutCertificate())
            && data.getMarriageOutsideOfUK().equalsIgnoreCase("yes")
            && data.getMakingAnApplicationWithoutCertificate().equalsIgnoreCase("yes")
        ) {
            validateFields.put("placeOfMarriageOrCivilPartnership", data.getPlaceOfMarriageOrCivilPartnership());
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
            validateFields.put("dateOfMarriageOrCivilPartnershipDay", data.getDateOfMarriageOrCivilPartnershipDay());
            validateFields.put("dateOfMarriageOrCivilPartnershipMonth", data.getDateOfMarriageOrCivilPartnershipMonth());
            validateFields.put("dateOfMarriageOrCivilPartnershipYear", data.getDateOfMarriageOrCivilPartnershipYear());
        }

        validateFields.put("soleOrApplicant1FullNameAsOnCert", data.getSoleOrApplicant1FullNameAsOnCert());
        validateFields.put("respondentOrApplicant2FullNameAsOnCert", data.getRespondentOrApplicant2FullNameAsOnCert());
        validateFields.put("detailsOnCertCorrect", data.getDetailsOnCertCorrect());

        if (!isEmpty(data.getDetailsOnCertCorrect())
            && data.getDetailsOnCertCorrect().equalsIgnoreCase("no")
        ) {
            validateFields.put("reasonWhyCertNotCorrect", data.getReasonWhyCertNotCorrect());
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateJurisdiction(OcrDataFields data, List<String> warnings, List<String> errors) {

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

    private void validateStatementOfIrretrievableBreakdown(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1ConfirmationOfBreakdown", data.getSoleOrApplicant1ConfirmationOfBreakdown());
        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));

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

    private void validateExistingCourtCases(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        if (!isEmpty(data.getExistingOrPreviousCourtCases())
            && data.getExistingOrPreviousCourtCases().equalsIgnoreCase("yes")
        ) {
            validateFields.put("existingOrPreviousCourtCaseNumbers", data.getExistingOrPreviousCourtCaseNumbers());
            validateFields.put("summaryOfExistingOrPreviousCourtCases", data.getSummaryOfExistingOrPreviousCourtCases());

            validateFields.entrySet().stream()
                .filter(e -> isEmpty(e.getValue()))
                .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
        }
    }

    private void validateMoneyPropertyAndPrayer(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        if (!isEmpty(data.getSoleOrApplicant1FinancialOrder())
            && data.getSoleOrApplicant1FinancialOrder().equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1FinancialOrderFor", data.getSoleOrApplicant1FinancialOrderFor());
        }

        if (!isEmpty(data.getApplicant2FinancialOrder())
            && data.getApplicant2FinancialOrder().equalsIgnoreCase("yes")
        ) {
            validateFields.put("applicant2FinancialOrderFor", data.getApplicant2FinancialOrderFor());
        }

        if (isEmpty(data.getPrayerMarriageDissolved()) && isEmpty(data.getPrayerCivilPartnershipDissolved())) {
            warnings.add("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
        }

        if (!isEmpty(data.getSoleOrApplicant1FinancialOrder())
            && data.getSoleOrApplicant1FinancialOrder().equalsIgnoreCase("yes")
        ) {
            validateFields.put("soleOrApplicant1prayerFinancialOrder", data.getSoleOrApplicant1prayerFinancialOrder());
            validateFields.put("soleOrApplicant1prayerFinancialOrderFor", data.getSoleOrApplicant1prayerFinancialOrderFor());
            validateFields.put("applicant2PrayerFinancialOrder", data.getApplicant2PrayerFinancialOrder());
            validateFields.put("applicant2PrayerFinancialOrderFor", data.getApplicant2PrayerFinancialOrderFor());
        } else if (!isEmpty(data.getSoleOrApplicant1FinancialOrder())
            && data.getSoleOrApplicant1FinancialOrder().equalsIgnoreCase("no")
        ) {
            if (!isEmpty(data.getSoleOrApplicant1prayerFinancialOrder())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrder"));
            }
            if (!isEmpty(data.getSoleOrApplicant1prayerFinancialOrderFor())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrderFor"));
            }
            if (!isEmpty(data.getApplicant2PrayerFinancialOrder())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrder"));
            }
            if (!isEmpty(data.getApplicant2PrayerFinancialOrderFor())) {
                warnings.add(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrderFor"));
            }
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateSoT(OcrDataFields data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleApplicantOrApplicant1StatementOfTruth",data.getSoleApplicantOrApplicant1StatementOfTruth());
        validateFields.put("soleApplicantOrApplicant1LegalRepStatementOfTruth",
            data.getSoleApplicantOrApplicant1LegalRepStatementOfTruth());
        validateFields.put("soleApplicantOrApplicant1OrLegalRepSignature", data.getSoleApplicantOrApplicant1OrLegalRepSignature());
        validateFields.put("soleApplicantOrApplicant1Signing",data.getSoleApplicantOrApplicant1Signing());
        validateFields.put("legalRepSigning",data.getLegalRepSigning());
        validateFields.put("statementOfTruthDateDay",data.getStatementOfTruthDateDay());
        validateFields.put("statementOfTruthDateMonth",data.getStatementOfTruthDateMonth());
        validateFields.put("statementOfTruthDateYear",data.getStatementOfTruthDateYear());
        validateFields.put("soleApplicantOrApplicant1OrLegalRepFullName",data.getSoleApplicantOrApplicant1OrLegalRepFullName());

        if (!isEmpty(data.getJointApplication())
            && data.getJointApplication().equalsIgnoreCase("true")
        ) {
            validateFields.put("applicant2StatementOfTruth", data.getApplicant2StatementOfTruth());
            validateFields.put("applicant2LegalRepStatementOfTruth", data.getApplicant2LegalRepStatementOfTruth());
            validateFields.put("applicant2OrLegalRepSignature", data.getApplicant2OrLegalRepSignature());
            validateFields.put("applicant2Signing", data.getApplicant2Signing());
            validateFields.put("applicant2LegalRepSigning", data.getApplicant2LegalRepSigning());
            validateFields.put("applicant2StatementOfTruthDateDay", data.getApplicant2StatementOfTruthDateDay());
            validateFields.put("applicant2StatementOfTruthDateMonth", data.getApplicant2StatementOfTruthDateMonth());
            validateFields.put("applicant2StatementOfTruthDateYear", data.getApplicant2StatementOfTruthDateYear());
            validateFields.put("applicant2OrLegalRepFullName", data.getApplicant2OrLegalRepFullName());
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));

        if (!isEmpty(data.getSoleOrApplicant1HWFNo())
            && data.getSoleOrApplicant1HWFNo().length() != 6
        ) {
            warnings.add("soleOrApplicant1HWFNo should be 6 digits long");
        }
    }
}
