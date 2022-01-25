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
        validateMoneyAndProperty(data, warnings, errors);
        validatePrayer(data, warnings, errors);
        validateSoT(data, warnings, errors);

        return OcrValidationResponse.builder()
            .errors(errors)
            .warnings(warnings)
            .status(getValidationStatus(errors, warnings))
            .build();
    }

    private void validateYourApplication(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("applicationForDivorce", (String) data.get("applicationForDivorce"));
        validateFields.put("applicationForDissolution", (String) data.get("applicationForDissolution"));
        validateFields.put("aSoleApplication", (String) data.get("aSoleApplication"));
        validateFields.put("aJointApplication", (String) data.get("aJointApplication"));
        validateFields.put("marriageOrCivilPartnershipCertificate", (String) data.get("marriageOrCivilPartnershipCertificate"));
        validateFields.put("translation", (String) data.get("translation"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validateAboutYou(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1FirstName", (String) data.get("soleOrApplicant1FirstName"));
        validateFields.put("soleOrApplicant1MiddleName", (String) data.get("soleOrApplicant1MiddleName"));
        validateFields.put("soleApplicantOrApplicant1LastName", (String) data.get("soleApplicantOrApplicant1LastName"));
        validateFields.put("soleOrApplicant1MarriedName", (String) data.get("soleOrApplicant1MarriedName"));
        validateFields.put("soleOrApplicant1MarriedNameReason", (String) data.get("soleOrApplicant1MarriedNameReason"));
        validateFields.put("confidentialDetailsSpouseOrCivilPartner", (String) data.get("confidentialDetailsSpouseOrCivilPartner"));
        validateFields.put("soleOrApplicant1BuildingAndStreet", (String) data.get("soleOrApplicant1BuildingAndStreet"));
        validateFields.put("soleOrApplicant1SecondLineOfAddress", (String) data.get("soleOrApplicant1SecondLineOfAddress"));
        validateFields.put("soleOrApplicant1TownOrCity", (String) data.get("soleOrApplicant1TownOrCity"));
        validateFields.put("soleOrApplicant1County", (String) data.get("soleOrApplicant1County"));
        validateFields.put("soleOrApplicant1Postcode", (String) data.get("soleOrApplicant1Postcode"));
        validateFields.put("soleOrApplicant1Country", (String) data.get("soleOrApplicant1Country"));
        validateFields.put("soleOrApplicant1PhoneNo", (String) data.get("soleOrApplicant1PhoneNo"));
        validateFields.put("soleOrApplicant1Email", (String) data.get("soleOrApplicant1Email"));
        validateFields.put("soleOrApplicant1Solicitor", (String) data.get("soleOrApplicant1Solicitor"));
        validateFields.put("soleOrApplicant1SolicitorName", (String) data.get("soleOrApplicant1SolicitorName"));
        validateFields.put("soleOrApplicant1SolicitorRefNo", (String) data.get("soleOrApplicant1SolicitorRefNo"));
        validateFields.put("soleOrApplicant1SolicitorFirm", (String) data.get("soleOrApplicant1SolicitorFirm"));
        validateFields.put("soleOrApplicant1SolicitorBuildingAndStreet", (String) data.get("soleOrApplicant1SolicitorBuildingAndStreet"));
        validateFields.put("soleOrApplicant1SolicitorSecondLineOfAddress", (String) data.get("soleOrApplicant1SolicitorSecondLineOfAddress"));
        validateFields.put("soleOrApplicant1SolicitorTownOrCity", (String) data.get("soleOrApplicant1SolicitorTownOrCity"));
        validateFields.put("soleOrApplicant1SolicitorCounty", (String) data.get("soleOrApplicant1SolicitorCounty"));
        validateFields.put("soleOrApplicant1SolicitorCountry", (String) data.get("soleOrApplicant1SolicitorCountry"));
        validateFields.put("soleOrApplicant1SolicitorPostcode", (String) data.get("soleOrApplicant1SolicitorPostcode"));
        validateFields.put("soleOrApplicant1SolicitorDX", (String) data.get("soleOrApplicant1SolicitorDX"));
        validateFields.put("soleOrApplicant1SolicitorPhoneNo", (String) data.get("soleOrApplicant1SolicitorPhoneNo"));
        validateFields.put("soleOrApplicant1SolicitorEmail", (String) data.get("soleOrApplicant1SolicitorEmail"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validateAboutTheRespondent(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();

        validateFields.put("respondentOrApplicant2FirstName", (String) data.get("respondentOrApplicant2FirstName"));
        validateFields.put("respondentOrApplicant2MiddleName", (String) data.get("respondentOrApplicant2MiddleName"));
        validateFields.put("respondentOrApplicant2LastName", (String) data.get("respondentOrApplicant2LastName"));
        validateFields.put("respondentOrApplicant2MarriedName", (String) data.get("respondentOrApplicant2MarriedName"));
        validateFields.put("respondentOrApplicant2WhyMarriedNameChanged", (String) data.get("respondentOrApplicant2WhyMarriedNameChanged"));
        validateFields.put("respondentOrApplicant2BuildingAndStreet", (String) data.get("respondentOrApplicant2BuildingAndStreet"));
        validateFields.put("respondentOrApplicant2SecondLineOfAddress", (String) data.get("respondentOrApplicant2SecondLineOfAddress"));
        validateFields.put("respondentOrApplicant2TownOrCity", (String) data.get("respondentOrApplicant2TownOrCity"));
        validateFields.put("respondentOrApplicant2County", (String) data.get("respondentOrApplicant2County"));
        validateFields.put("respondentOrApplicant2Country", (String) data.get("respondentOrApplicant2Country"));
        validateFields.put("respondentOrApplicant2Postcode", (String) data.get("respondentOrApplicant2Postcode"));
        validateFields.put("respondentOrApplicant2PhoneNo", (String) data.get("respondentOrApplicant2PhoneNo"));
        validateFields.put("respondentOrApplicant2Email", (String) data.get("respondentOrApplicant2Email"));
        validateFields.put("respondentEmailAccess", (String) data.get("respondentEmailAccess"));

        //        if (is a joint application please go to respondentOrApplicant2SolicitorName and skip:
        //        respondentDifferentServiceAddress, serveOutOfUK, respondentServePostOnly, applicantWillServeApplication) {
        //
        //        }

        validateFields.put("serveOutOfUK", (String) data.get("serveOutOfUK"));
        validateFields.put("respondentServePostOnly", (String) data.get("respondentServePostOnly"));
        validateFields.put("applicantWillServeApplication", (String) data.get("applicantWillServeApplication"));
        validateFields.put("respondentDifferentServiceAddress", (String) data.get("respondentDifferentServiceAddress"));


        validateFields.put("respondentOrApplicant2SolicitorName", (String) data.get("respondentOrApplicant2SolicitorName"));
        validateFields.put("respondentOrApplicant2SolicitorRefNo", (String) data.get("respondentOrApplicant2SolicitorRefNo"));
        validateFields.put("respondentOrApplicant2SolicitorFirm", (String) data.get("respondentOrApplicant2SolicitorFirm"));
        validateFields.put("respondentOrApplicant2SolicitorBuildingAndStreet", (String) data.get("respondentOrApplicant2SolicitorBuildingAndStreet"));
        validateFields.put("respondentOrApplicant2SolicitorSecondLineOfAddress", (String) data.get("respondentOrApplicant2SolicitorSecondLineOfAddress"));
        validateFields.put("respondentOrApplicant2SolicitorTownOrCity", (String) data.get("respondentOrApplicant2SolicitorTownOrCity"));
        validateFields.put("respondentOrApplicant2SolicitorCounty", (String) data.get("respondentOrApplicant2SolicitorCounty"));
        validateFields.put("respondentOrApplicant2SolicitorCountry", (String) data.get("respondentOrApplicant2SolicitorCountry"));
        validateFields.put("respondentOrApplicant2SolicitorPostcode", (String) data.get("respondentOrApplicant2SolicitorPostcode"));
        validateFields.put("respondentOrApplicant2SolicitorDX", (String) data.get("respondentOrApplicant2SolicitorDX"));
        validateFields.put("applicant2SolicitorPhone", (String) data.get("applicant2SolicitorPhone"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
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
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
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
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validateStatementOfIrretrievableBreakdown(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1ConfirmationOfBreakdown", (String) data.get("soleOrApplicant1ConfirmationOfBreakdown"));
        validateFields.put("applicant2ConfirmationOfBreakdown", (String) data.get("applicant2ConfirmationOfBreakdown"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validateExistingCourtCases(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("existingOrPreviousCourtCases", (String) data.get("existingOrPreviousCourtCases"));
        validateFields.put("existingOrPreviousCourtCaseNumbers", (String) data.get("existingOrPreviousCourtCaseNumbers"));
        validateFields.put("summaryOfExistingOrPreviousCourtCases", (String) data.get("summaryOfExistingOrPreviousCourtCases"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validateMoneyAndProperty(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleOrApplicant1FinancialOrder", (String) data.get("soleOrApplicant1FinancialOrder"));
        validateFields.put("soleOrApplicant1FinancialOrderFor", (String) data.get("soleOrApplicant1FinancialOrderFor"));
        validateFields.put("applicant2FinancialOrder", (String) data.get("applicant2FinancialOrder"));
        validateFields.put("applicant2FinancialOrderFor", (String) data.get("applicant2FinancialOrderFor"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validatePrayer(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("prayerMarriageDissolved", (String) data.get("prayerMarriageDissolved"));
        validateFields.put("prayerCivilPartnershipDissolved", (String) data.get("prayerCivilPartnershipDissolved"));
        validateFields.put("soleOrApplicant1prayerFinancialOrder", (String) data.get("soleOrApplicant1prayerFinancialOrder"));
        validateFields.put("soleOrApplicant1prayerFinancialOrderFor", (String) data.get("soleOrApplicant1prayerFinancialOrderFor"));
        validateFields.put("applicant2PrayerFinancialOrder", (String) data.get("applicant2PrayerFinancialOrder"));
        validateFields.put("applicant2PrayerFinancialOrderFor", (String) data.get("applicant2PrayerFinancialOrderFor"));

        validateFields.entrySet().stream()
            .filter(e -> isEmpty(e.getValue()))
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
    }

    private void validateSoT(Map<String, Object> data, List<String> warnings, List<String> errors) {

        Map<String, String> validateFields = new HashMap<>();
        validateFields.put("soleApplicantOrApplicant1StatementOfTruth", (String) data.get("soleApplicantOrApplicant1StatementOfTruth"));
        validateFields.put("soleApplicantOrApplicant1LegalRepStatementOfTruth", (String) data.get("soleApplicantOrApplicant1LegalRepStatementOfTruth"));
        validateFields.put("soleApplicantOrApplicant1OrLegalRepSignature", (String) data.get("soleApplicantOrApplicant1OrLegalRepSignature"));
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
            .forEach(e -> warnings.add(String.format("Field is empty or missing: %s", e.getKey())));
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
