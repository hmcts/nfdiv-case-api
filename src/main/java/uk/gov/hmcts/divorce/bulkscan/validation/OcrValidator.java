package uk.gov.hmcts.divorce.bulkscan.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.data.KeyValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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

    private void validateYourApplication(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 1

        String applicationForDivorce = (String) data.get("applicationForDivorce");
        String applicationForDissolution = (String) data.get("applicationForDissolution");
        String aSoleApplication = (String) data.get("aSoleApplication");
        String aJointApplication = (String) data.get("aJointApplication");
        String marriageOrCivilPartnershipCertificate = (String) data.get("marriageOrCivilPartnershipCertificate");
        String translation = (String) data.get("translation");

        // If field is empty validation status = warning
    }

    private void validateAboutYou(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 2

        String soleOrApplicant1FirstName = (String) data.get("soleOrApplicant1FirstName");
        String soleOrApplicant1MiddleName = (String) data.get("soleOrApplicant1MiddleName");
        String soleApplicantOrApplicant1LastName = (String) data.get("soleApplicantOrApplicant1LastName");
        String soleOrApplicant1MarriedName = (String) data.get("soleOrApplicant1MarriedName");
        String soleOrApplicant1MarriedNameReason = (String) data.get("soleOrApplicant1MarriedNameReason");
        String confidentialDetailsSpouseOrCivilPartner = (String) data.get("confidentialDetailsSpouseOrCivilPartner");
        String soleOrApplicant1BuildingAndStreet = (String) data.get("soleOrApplicant1BuildingAndStreet");
        String soleOrApplicant1SecondLineOfAddress = (String) data.get("soleOrApplicant1SecondLineOfAddress");
        String soleOrApplicant1TownOrCity = (String) data.get("soleOrApplicant1TownOrCity");
        String soleOrApplicant1County = (String) data.get("soleOrApplicant1County");
        String soleOrApplicant1Postcode = (String) data.get("soleOrApplicant1Postcode");
        String soleOrApplicant1Country = (String) data.get("soleOrApplicant1Country");
        String soleOrApplicant1PhoneNo = (String) data.get("soleOrApplicant1PhoneNo");
        String soleOrApplicant1Email = (String) data.get("soleOrApplicant1Email");
        String soleOrApplicant1Solicitor = (String) data.get("soleOrApplicant1Solicitor");
        String soleOrApplicant1SolicitorName = (String) data.get("soleOrApplicant1SolicitorName");
        String soleOrApplicant1SolicitorRefNo = (String) data.get("soleOrApplicant1SolicitorRefNo");
        String soleOrApplicant1SolicitorFirm = (String) data.get("soleOrApplicant1SolicitorFirm");
        String soleOrApplicant1SolicitorBuildingAndStreet = (String) data.get("soleOrApplicant1SolicitorBuildingAndStreet");
        String soleOrApplicant1SolicitorSecondLineOfAddress = (String) data.get("soleOrApplicant1SolicitorSecondLineOfAddress");
        String soleOrApplicant1SolicitorTownOrCity = (String) data.get("soleOrApplicant1SolicitorTownOrCity");
        String soleOrApplicant1SolicitorCounty = (String) data.get("soleOrApplicant1SolicitorCounty");
        String soleOrApplicant1SolicitorCountry = (String) data.get("soleOrApplicant1SolicitorCountry");
        String soleOrApplicant1SolicitorPostcode = (String) data.get("soleOrApplicant1SolicitorPostcode");
        String soleOrApplicant1SolicitorDX = (String) data.get("soleOrApplicant1SolicitorDX");
        String soleOrApplicant1SolicitorPhoneNo = (String) data.get("soleOrApplicant1SolicitorPhoneNo");
        String soleOrApplicant1SolicitorEmail = (String) data.get("soleOrApplicant1SolicitorEmail");

        // If field is empty validation status = warning
    }

    private void validateAboutTheRespondent(Map<String, Object> data, List<String> warnings, List<String> errors) {
        // Section 3

        String respondentOrApplicant2FirstName = (String) data.get("respondentOrApplicant2FirstName");
        String respondentOrApplicant2MiddleName = (String) data.get("respondentOrApplicant2MiddleName");
        String respondentOrApplicant2LastName = (String) data.get("respondentOrApplicant2LastName");
        String respondentOrApplicant2MarriedName = (String) data.get("respondentOrApplicant2MarriedName");
        String respondentOrApplicant2WhyMarriedNameChanged = (String) data.get("respondentOrApplicant2WhyMarriedNameChanged");
        String respondentOrApplicant2BuildingAndStreet = (String) data.get("respondentOrApplicant2BuildingAndStreet");
        String respondentOrApplicant2SecondLineOfAddress = (String) data.get("respondentOrApplicant2SecondLineOfAddress");
        String respondentOrApplicant2TownOrCity = (String) data.get("respondentOrApplicant2TownOrCity");
        String respondentOrApplicant2County = (String) data.get("respondentOrApplicant2County");
        String respondentOrApplicant2Country = (String) data.get("respondentOrApplicant2Country");
        String respondentOrApplicant2Postcode = (String) data.get("respondentOrApplicant2Postcode");
        String respondentOrApplicant2PhoneNo = (String) data.get("respondentOrApplicant2PhoneNo");
        String respondentOrApplicant2Email = (String) data.get("respondentOrApplicant2Email");
        String respondentEmailAccess = (String) data.get("respondentEmailAccess");

        //        if (is a joint application please go to respondentOrApplicant2SolicitorName and skip:
        //        respondentDifferentServiceAddress, serveOutOfUK, respondentServePostOnly, applicantWillServeApplication) {
//
//        }

        String serveOutOfUK = (String) data.get("serveOutOfUK");
        String respondentServePostOnly = (String) data.get("respondentServePostOnly");
        String applicantWillServeApplication = (String) data.get("applicantWillServeApplication");
        String respondentDifferentServiceAddress = (String) data.get("respondentDifferentServiceAddress");


        String respondentOrApplicant2SolicitorName = (String) data.get("respondentOrApplicant2SolicitorName");
        String respondentOrApplicant2SolicitorRefNo = (String) data.get("respondentOrApplicant2SolicitorRefNo");
        String respondentOrApplicant2SolicitorFirm = (String) data.get("respondentOrApplicant2SolicitorFirm");
        String respondentOrApplicant2SolicitorBuildingAndStreet = (String) data.get("respondentOrApplicant2SolicitorBuildingAndStreet");
        String respondentOrApplicant2SolicitorSecondLineOfAddress = (String) data.get("respondentOrApplicant2SolicitorSecondLineOfAddress");
        String respondentOrApplicant2SolicitorTownOrCity = (String) data.get("respondentOrApplicant2SolicitorTownOrCity");
        String respondentOrApplicant2SolicitorCounty = (String) data.get("respondentOrApplicant2SolicitorCounty");
        String respondentOrApplicant2SolicitorCountry = (String) data.get("respondentOrApplicant2SolicitorCountry");
        String respondentOrApplicant2SolicitorPostcode = (String) data.get("respondentOrApplicant2SolicitorPostcode");
        String respondentOrApplicant2SolicitorDX = (String) data.get("respondentOrApplicant2SolicitorDX");
        String applicant2SolicitorPhone = (String) data.get("applicant2SolicitorPhone");

        // If field is empty validation status = warning
    }

    private void validateDetailsOfUnion(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 4

        String MarriageOutsideOfUK = (String) data.get("MarriageOutsideOfUK");
        String makingAnApplicationWithoutCertificate = (String) data.get("makingAnApplicationWithoutCertificate");
        String placeOfMarriageOrCivilPartnership = (String) data.get("placeOfMarriageOrCivilPartnership");
        String dateOfMarriageOrCivilPartnershipDay = (String) data.get("dateOfMarriageOrCivilPartnershipDay");
        String dateOfMarriageOrCivilPartnershipMonth = (String) data.get("dateOfMarriageOrCivilPartnershipMonth");
        String dateOfMarriageOrCivilPartnershipYear = (String) data.get("dateOfMarriageOrCivilPartnershipYear");
        String soleOrApplicant1FullNameAsOnCert = (String) data.get("soleOrApplicant1FullNameAsOnCert");
        String respondentOrApplicant2FullNameAsOnCert = (String) data.get("respondentOrApplicant2FullNameAsOnCert");
        String detailsOnCertCorrect = (String) data.get("detailsOnCertCorrect");
        String reasonWhyCertNotCorrect = (String) data.get("reasonWhyCertNotCorrect");

        // If field is empty validation status = warning
    }

    private void validateJurisdiction(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 5

        String jurisdictionReasonsBothPartiesHabitual = (String) data.get("jurisdictionReasonsBothPartiesHabitual");
        String jurisdictionReasonsBothPartiesLastHabitual = (String) data.get("jurisdictionReasonsBothPartiesLastHabitual");
        String jurisdictionReasonsRespHabitual = (String) data.get("jurisdictionReasonsRespHabitual");
        String jurisdictionReasonsJointHabitual = (String) data.get("jurisdictionReasonsJointHabitual");
        String jurisdictionReasonsJointHabitualWho = (String) data.get("jurisdictionReasonsJointHabitualWho");
        String jurisdictionReasons1YrHabitual = (String) data.get("jurisdictionReasons1YrHabitual");
        String jurisdictionReasons6MonthsHabitual = (String) data.get("jurisdictionReasons6MonthsHabitual");
        String jurisdictionReasonsBothPartiesDomiciled = (String) data.get("jurisdictionReasonsBothPartiesDomiciled");
        String jurisdictionReasonsOnePartyDomiciled = (String) data.get("jurisdictionReasonsOnePartyDomiciled");

        // if above has entry and below is empty then that is SUCCESS
        // if above has no entry and below is empty then that is WARNING
        String jurisdictionReasonsSameSex = (String) data.get("jurisdictionReasonsSameSex");

        // If field is empty validation status = warning
    }

    private void validateStatementOfIrretrievableBreakdown(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 6

        String soleOrApplicant1ConfirmationOfBreakdown = (String) data.get("soleOrApplicant1ConfirmationOfBreakdown");
        String applicant2ConfirmationOfBreakdown = (String) data.get("applicant2ConfirmationOfBreakdown");

        // If field is empty validation status = warning
    }

    private void validateExistingCourtCases(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 7

        String existingOrPreviousCourtCases = (String) data.get("existingOrPreviousCourtCases");
        String existingOrPreviousCourtCaseNumbers = (String) data.get("existingOrPreviousCourtCaseNumbers");
        String summaryOfExistingOrPreviousCourtCases = (String) data.get("summaryOfExistingOrPreviousCourtCases");

        // If field is empty validation status = warning
    }

    private void validateMoneyAndProperty(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 8

        String soleOrApplicant1FinancialOrder = (String) data.get("soleOrApplicant1FinancialOrder");
        String soleOrApplicant1FinancialOrderFor = (String) data.get("soleOrApplicant1FinancialOrderFor");
        String applicant2FinancialOrder = (String) data.get("applicant2FinancialOrder");
        String applicant2FinancialOrderFor = (String) data.get("applicant2FinancialOrderFor");

        // If field is empty validation status = warning
    }

    private void validatePrayer(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 9
        String prayerMarriageDissolved = (String) data.get("prayerMarriageDissolved");
        String prayerCivilPartnershipDissolved = (String) data.get("prayerCivilPartnershipDissolved");
        String soleOrApplicant1prayerFinancialOrder = (String) data.get("soleOrApplicant1prayerFinancialOrder");
        String soleOrApplicant1prayerFinancialOrderFor = (String) data.get("soleOrApplicant1prayerFinancialOrderFor");
        String applicant2PrayerFinancialOrder = (String) data.get("applicant2PrayerFinancialOrder");
        String applicant2PrayerFinancialOrderFor = (String) data.get("applicant2PrayerFinancialOrderFor");



        // If field is empty validation status = warning
    }

    private void validateSoT(Map<String, Object> data, List<String> warnings, List<String> errors) {

        // Section 10

        String soleApplicantOrApplicant1StatementOfTruth = (String) data.get("soleApplicantOrApplicant1StatementOfTruth");
        String soleApplicantOrApplicant1LegalRepStatementOfTruth = (String) data.get("soleApplicantOrApplicant1LegalRepStatementOfTruth");
        String soleApplicantOrApplicant1OrLegalRepSignature = (String) data.get("soleApplicantOrApplicant1OrLegalRepSignature");
        String soleApplicantOrApplicant1Signing = (String) data.get("soleApplicantOrApplicant1Signing");
        String legalRepSigning = (String) data.get("legalRepSigning");
        String statementOfTruthDateDay = (String) data.get("statementOfTruthDateDay");
        String statementOfTruthDateMonth = (String) data.get("statementOfTruthDateMonth");
        String statementOfTruthDateYear = (String) data.get("statementOfTruthDateYear");
        String soleApplicantOrApplicant1OrLegalRepFullName = (String) data.get("soleApplicantOrApplicant1OrLegalRepFullName");
        String soleApplicantOrApplicant1LegalRepFirm = (String) data.get("soleApplicantOrApplicant1LegalRepFirm");
        String soleApplicantOrApplicant1LegalRepPosition = (String) data.get("soleApplicantOrApplicant1LegalRepPosition");
        String applicant2StatementOfTruth = (String) data.get("applicant2StatementOfTruth");
        String applicant2LegalRepStatementOfTruth = (String) data.get("applicant2LegalRepStatementOfTruth");
        String applicant2OrLegalRepSignature = (String) data.get("applicant2OrLegalRepSignature");
        String applicant2StatementOfTruthDateDay = (String) data.get("applicant2StatementOfTruthDateDay");
        String applicant2StatementOfTruthDateMonth = (String) data.get("applicant2StatementOfTruthDateMonth");
        String applicant2StatementOfTruthDateYear = (String) data.get("applicant2StatementOfTruthDateYear");
        String applicant2OrLegalRepFullName = (String) data.get("applicant2OrLegalRepFullName");
        String applicant2LegalRepFirm = (String) data.get("applicant2LegalRepFirm");
        String courtFee = (String) data.get("courtFee");

        String soleOrApplicant1NoPaymentIncluded = (String) data.get("soleOrApplicant1NoPaymentIncluded");
        String soleOrApplicant1HWFConfirmation = (String) data.get("soleOrApplicant1HWFConfirmation");
        String soleOrApplicant1HWFNo = (String) data.get("soleOrApplicant1HWFNo");
        String soleOrApplicant1HWFApp = (String) data.get("soleOrApplicant1HWFApp");
        String soleOrApplicant1PaymentOther = (String) data.get("soleOrApplicant1PaymentOther");
        String soleOrApplicant1PaymentOtherDetail = (String) data.get("soleOrApplicant1PaymentOtherDetail");

        String applicant2NoPaymentIncluded = (String) data.get("applicant2NoPaymentIncluded");
        String applicant2HWFConfirmation = (String) data.get("applicant2HWFConfirmation");
        String applicant2HWFConfirmationNo = (String) data.get("applicant2HWFConfirmationNo");
        String applicant2HWFApp = (String) data.get("applicant2HWFApp");
        String applicant2PaymentOther = (String) data.get("applicant2PaymentOther");

        String debitCreditCardPayment = (String) data.get("debitCreditCardPayment");
        String debitCreditCardPaymentPhone = (String) data.get("debitCreditCardPaymentPhone");
        String paymentDetailEmail = (String) data.get("paymentDetailEmail");
        String chequeOrPostalOrderPayment = (String) data.get("chequeOrPostalOrderPayment");

        // If field is empty validation status = warning
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
