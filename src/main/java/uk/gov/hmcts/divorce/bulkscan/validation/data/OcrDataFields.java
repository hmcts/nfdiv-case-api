
package uk.gov.hmcts.divorce.bulkscan.validation.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OcrDataFields {

    private String applicationForDivorce;
    private String applicationForDissolution;
    @JsonProperty("aSoleApplication")
    private String soleApplication;
    @JsonProperty("aJointApplication")
    private String jointApplication;
    private String marriageOrCivilPartnershipCertificate;
    private String translation;
    private String soleApplicantOrApplicant1FirstName;
    private String soleApplicantOrApplicant1MiddleName;
    private String soleApplicantOrApplicant1LastName;
    private String soleOrApplicant1MarriedName;
    private String soleOrApplicant1MarriedNameReason;
    private String confidentialDetailsSpouseOrCivilPartner;
    private String soleOrApplicant1BuildingAndStreet;
    private String soleOrApplicant1SecondLineOfAddress;
    private String soleOrApplicant1TownOrCity;
    private String soleOrApplicant1County;
    private String soleOrApplicant1Postcode;
    private String soleOrApplicant1Country;
    private String soleOrApplicant1PhoneNo;
    private String soleOrApplicant1Email;
    private String soleOrApplicant1Solicitor;
    private String soleOrApplicant1SolicitorName;
    private String soleOrApplicant1SolicitorRefNo;
    private String soleOrApplicant1SolicitorFirm;
    private String soleOrApplicant1SolicitorBuildingAndStreet;
    private String soleOrApplicant1SolicitorSecondLineOfAddress;
    private String soleOrApplicant1SolicitorTownOrCity;
    private String soleOrApplicant1SolicitorCounty;
    private String soleOrApplicant1SolicitorCountry;
    private String soleOrApplicant1SolicitorPostcode;
    private String soleOrApplicant1SolicitorDX;
    private String soleOrApplicant1SolicitorPhoneNo;
    private String soleOrApplicant1SolicitorEmail;
    private String respondentOrApplicant2FirstName;
    private String respondentOrApplicant2MiddleName;
    private String respondentOrApplicant2LastName;
    private String respondentOrApplicant2MarriedName;
    private String respondentOrApplicant2WhyMarriedNameChanged;
    private String respondentOrApplicant2BuildingAndStreet;
    private String respondentOrApplicant2SecondLineOfAddress;
    private String respondentOrApplicant2TownOrCity;
    private String respondentOrApplicant2County;
    private String respondentOrApplicant2Country;
    private String respondentOrApplicant2Postcode;
    private String respondentOrApplicant2PhoneNo;
    private String respondentOrApplicant2Email;
    private String respondentEmailAccess;
    private String serveOutOfUK;
    private String respondentServePostOnly;
    private String applicantWillServeApplication;
    private String respondentDifferentServiceAddress;
    private String respondentOrApplicant2SolicitorName;
    private String respondentOrApplicant2SolicitorRefNo;
    private String respondentOrApplicant2SolicitorFirm;
    private String respondentOrApplicant2SolicitorBuildingAndStreet;
    private String respondentOrApplicant2SolicitorSecondLineOfAddress;
    private String respondentOrApplicant2SolicitorTownOrCity;
    private String respondentOrApplicant2SolicitorCounty;
    private String respondentOrApplicant2SolicitorCountry;
    private String respondentOrApplicant2SolicitorPostcode;
    private String respondentOrApplicant2SolicitorDX;
    private String applicant2SolicitorPhone;
    private String marriageOutsideOfUK;
    private String makingAnApplicationWithoutCertificate;
    private String placeOfMarriageOrCivilPartnership;
    private String dateOfMarriageOrCivilPartnershipDay;
    private String dateOfMarriageOrCivilPartnershipMonth;
    private String dateOfMarriageOrCivilPartnershipYear;
    private String soleOrApplicant1FullNameAsOnCert;
    private String respondentOrApplicant2FullNameAsOnCert;
    private String detailsOnCertCorrect;
    private String reasonWhyCertNotCorrect;
    private String jurisdictionReasonsBothPartiesHabitual;
    private String jurisdictionReasonsBothPartiesLastHabitual;
    private String jurisdictionReasonsRespHabitual;
    private String jurisdictionReasonsJointHabitual;
    private String jurisdictionReasonsJointHabitualWho;
    private String jurisdictionReasons1YrHabitual;
    private String jurisdictionReasons6MonthsHabitual;
    private String jurisdictionReasonsBothPartiesDomiciled;
    private String jurisdictionReasonsOnePartyDomiciled;
    private String jurisdictionReasonsOnePartyDomiciledWho;
    private String jurisdictionReasonsSameSex;
    private String soleOrApplicant1ConfirmationOfBreakdown;
    private String applicant2ConfirmationOfBreakdown;
    private String existingOrPreviousCourtCases;
    private String existingOrPreviousCourtCaseNumbers;
    private String summaryOfExistingOrPreviousCourtCases;
    private String soleOrApplicant1FinancialOrder;
    private String soleOrApplicant1FinancialOrderFor;
    private String applicant2FinancialOrder;
    private String applicant2FinancialOrderFor;
    private String prayerMarriageDissolved;
    private String prayerCivilPartnershipDissolved;
    private String prayerApplicant1JudiciallySeparated;
    private String soleOrApplicant1prayerFinancialOrder;
    private String soleOrApplicant1prayerFinancialOrderFor;
    private String applicant2PrayerFinancialOrder;
    private String applicant2PrayerFinancialOrderFor;
    private String soleApplicantOrApplicant1StatementOfTruth;
    private String soleApplicantOrApplicant1LegalRepStatementOfTruth;
    private String soleApplicantOrApplicant1OrLegalRepSignature;
    private String soleApplicantOrApplicant1Signing;
    private String legalRepSigning;
    private String statementOfTruthDateDay;
    private String statementOfTruthDateMonth;
    private String statementOfTruthDateYear;
    private String soleApplicantOrApplicant1OrLegalRepFullName;
    private String soleApplicantOrApplicant1LegalRepFirm;
    private String soleApplicantOrApplicant1LegalRepPosition;
    private String applicant2StatementOfTruth;
    private String applicant2LegalRepStatementOfTruth;
    private String applicant2OrLegalRepSignature;
    private String applicant2Signing;
    private String applicant2LegalRepSigning;
    private String applicant2StatementOfTruthDateDay;
    private String applicant2StatementOfTruthDateMonth;
    private String applicant2StatementOfTruthDateYear;
    private String applicant2OrLegalRepFullName;
    private String applicant2LegalRepFirm;
    private String applicant2LegalRepPosition;
    private String applicant2PaymentOtherDetail;
    private String courtFee;
    private String soleOrApplicant1NoPaymentIncluded;
    private String soleOrApplicant1HWFConfirmation;
    private String soleOrApplicant1HWFNo;
    private String soleOrApplicant1HWFApp;
    private String soleOrApplicant1PaymentOther;
    private String soleOrApplicant1PaymentOtherDetail;
    private String applicant2NoPaymentIncluded;
    private String applicant2HWFConfirmation;
    private String applicant2HWFNo;
    private String applicant2HWFApp;
    private String applicant2PaymentOther;
    private String howToPayEmail;
    private String debitCreditCardPayment;
    private String debitCreditCardPaymentPhone;
    private String paymentDetailEmail;
    private String chequeOrPostalOrderPayment;

    public static OcrDataFields transformData(List<OcrDataField> ocrDataFields) {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> map =
            Optional.ofNullable(ocrDataFields)
                .orElse(emptyList())
                .stream()
                .collect(
                    toMap(
                        entry -> entry.getName(),
                        entry -> Optional.ofNullable(entry.getValue()).orElse("")
                    )
                );
        return mapper.convertValue(map, OcrDataFields.class);
    }

    public static OcrDataFields transformOcrMapToObject(List<OcrDataField> ocrDataFields) {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> map =
            Optional.ofNullable(ocrDataFields)
                .orElse(emptyList())
                .stream()
                .collect(
                    toMap(
                        OcrDataField::getName,
                        entry -> Optional.ofNullable(entry.getValue()).orElse("")
                    )
                );
        return mapper.convertValue(map, OcrDataFields.class);
    }
}
