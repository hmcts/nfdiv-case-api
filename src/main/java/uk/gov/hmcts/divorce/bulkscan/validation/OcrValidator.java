package uk.gov.hmcts.divorce.bulkscan.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.data.KeyValue;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.endpoint.data.ValidationStatus;

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

    private static final String FIELD_EMPTY_OR_MISSING = "Field is empty or missing: %s";
    private static final String WARNING_NOT_APPLYING_FINANCIAL_ORDER = "Field must be empty as not applying for financial order: %s";

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
            && (((String) data.get("respondentOrApplicant2MarriedName")).equalsIgnoreCase("yes"))
        ) {
            validateFields.put("respondentOrApplicant2WhyMarriedNameChanged", (String) data.get("respondentOrApplicant2WhyMarriedNameChanged"));
        }

        if (!isEmpty(data.get("respondentOrApplicant2Email"))) {
            validateFields.put("respondentEmailAccess", (String) data.get("respondentEmailAccess"));
        }

        if (isEmpty(data.get("aSoleApplication")) && isEmpty(data.get("aJointApplication"))) {
            validateFields.put("serveOutOfUK", (String) data.get("serveOutOfUK"));
            validateFields.put("respondentDifferentServiceAddress", (String) data.get("respondentDifferentServiceAddress"));

            if (isEmpty(data.get("respondentServePostOnly")) && isEmpty(data.get("applicantWillServeApplication"))) {
                validateFields.put("respondentServePostOnly", (String) data.get("respondentServePostOnly"));
                validateFields.put("applicantWillServeApplication", (String) data.get("applicantWillServeApplication"));
            }
        }

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateDetailsOfUnion(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        validateFields.put("marriageOutsideOfUK", (String) data.get("marriageOutsideOfUK"));
        validateFields.put("makingAnApplicationWithoutCertificate", (String) data.get("makingAnApplicationWithoutCertificate"));
        validateFields.put("placeOfMarriageOrCivilPartnership", (String) data.get("placeOfMarriageOrCivilPartnership"));
        validateFields.put("dateOfMarriageOrCivilPartnershipDay", (String) data.get("dateOfMarriageOrCivilPartnershipDay"));
        validateFields.put("dateOfMarriageOrCivilPartnershipMonth", (String) data.get("dateOfMarriageOrCivilPartnershipMonth"));
        validateFields.put("dateOfMarriageOrCivilPartnershipYear", (String) data.get("dateOfMarriageOrCivilPartnershipYear"));
        validateFields.put("soleOrApplicant1FullNameAsOnCert", (String) data.get("soleOrApplicant1FullNameAsOnCert"));
        validateFields.put("respondentOrApplicant2FullNameAsOnCert", (String) data.get("respondentOrApplicant2FullNameAsOnCert"));
        validateFields.put("detailsOnCertCorrect", (String) data.get("detailsOnCertCorrect"));
        validateFields.put("reasonWhyCertNotCorrect", (String) data.get("reasonWhyCertNotCorrect"));

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

        // if above has entry and below is empty then that is SUCCESS
        // if above has no entry and below is empty then that is WARNING
        validateFields.put("jurisdictionReasonsSameSex", (String) data.get("jurisdictionReasonsSameSex"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
    }

    private void validateStatementOfIrretrievableBreakdown(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1ConfirmationOfBreakdown", (String) data.get("soleOrApplicant1ConfirmationOfBreakdown"));
        validateFields.put("applicant2ConfirmationOfBreakdown", (String) data.get("applicant2ConfirmationOfBreakdown"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
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
        validateFields.put("soleApplicantOrApplicant1LegalRepFirm", (String) data.get("soleApplicantOrApplicant1LegalRepFirm"));
        validateFields.put("soleApplicantOrApplicant1LegalRepPosition", (String) data.get("soleApplicantOrApplicant1LegalRepPosition"));
        validateFields.put("applicant2StatementOfTruth", (String) data.get("applicant2StatementOfTruth"));
        validateFields.put("applicant2LegalRepStatementOfTruth", (String) data.get("applicant2LegalRepStatementOfTruth"));
        validateFields.put("applicant2OrLegalRepSignature", (String) data.get("applicant2OrLegalRepSignature"));
        validateFields.put("applicant2StatementOfTruthDateDay", (String) data.get("applicant2StatementOfTruthDateDay"));
        validateFields.put("applicant2StatementOfTruthDateMonth", (String) data.get("applicant2StatementOfTruthDateMonth"));
        validateFields.put("applicant2StatementOfTruthDateYear", (String) data.get("applicant2StatementOfTruthDateYear"));
        validateFields.put("applicant2OrLegalRepFullName", (String) data.get("applicant2OrLegalRepFullName"));
        validateFields.put("applicant2LegalRepFirm", (String) data.get("applicant2LegalRepFirm"));
        validateFields.put("courtFee", (String) data.get("courtFee"));

        validateFields.put("soleOrApplicant1NoPaymentIncluded", (String) data.get("soleOrApplicant1NoPaymentIncluded"));
        validateFields.put("soleOrApplicant1HWFConfirmation", (String) data.get("soleOrApplicant1HWFConfirmation"));
        validateFields.put("soleOrApplicant1HWFNo", (String) data.get("soleOrApplicant1HWFNo"));
        validateFields.put("soleOrApplicant1HWFApp", (String) data.get("soleOrApplicant1HWFApp"));
        validateFields.put("soleOrApplicant1PaymentOther", (String) data.get("soleOrApplicant1PaymentOther"));
        validateFields.put("soleOrApplicant1PaymentOtherDetail", (String) data.get("soleOrApplicant1PaymentOtherDetail"));

        validateFields.put("applicant2NoPaymentIncluded", (String) data.get("applicant2NoPaymentIncluded"));
        validateFields.put("applicant2HWFConfirmation", (String) data.get("applicant2HWFConfirmation"));
        validateFields.put("applicant2HWFConfirmationNo", (String) data.get("applicant2HWFConfirmationNo"));
        validateFields.put("applicant2HWFApp", (String) data.get("applicant2HWFApp"));
        validateFields.put("applicant2PaymentOther", (String) data.get("applicant2PaymentOther"));

        validateFields.put("debitCreditCardPayment", (String) data.get("debitCreditCardPayment"));
        validateFields.put("debitCreditCardPaymentPhone", (String) data.get("debitCreditCardPaymentPhone"));
        validateFields.put("paymentDetailEmail", (String) data.get("paymentDetailEmail"));
        validateFields.put("chequeOrPostalOrderPayment", (String) data.get("chequeOrPostalOrderPayment"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format(FIELD_EMPTY_OR_MISSING, e.getKey())));
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
