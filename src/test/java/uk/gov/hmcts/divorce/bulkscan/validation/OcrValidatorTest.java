package uk.gov.hmcts.divorce.bulkscan.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.bulkscan.endpoint.data.OcrValidationResponse;
import uk.gov.hmcts.divorce.bulkscan.endpoint.model.input.OcrDataField;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.divorce.bulkscan.endpoint.data.ValidationStatus.WARNINGS;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELDS_CANNOT_MATCH;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELD_EMPTY_OR_MISSING_ERROR;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELD_EMPTY_OR_MISSING_WARNING;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.WARNING_NOT_APPLYING_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateD8OcrDataFields;

@ExtendWith(MockitoExtension.class)
public class OcrValidatorTest {

    @InjectMocks
    private OcrValidator validator;

    @Test
    void shouldValidateOcrDataSuccessfully() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(populateD8OcrDataFields())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors()).hasSize(0);
        assertThat(response.getWarnings()).hasSize(0);
        assertThat(response.getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    void shouldValidateOcrDataSuccessfullyWhenOcrDataFieldValuesAreNull() {
        List<OcrDataField> ocrDataFields = populateD8OcrDataFields();
        ocrDataFields.add(new OcrDataField("howToPayEmail", "null"));
        ocrDataFields.add(new OcrDataField("debitCreditCardPayment", "null"));

        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(ocrDataFields)
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
        assertThat(response.getStatus()).isEqualTo(SUCCESS);
    }


    @Test
    void shouldReturnWarningsWhenNoOcrDataPassed() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings()).hasSize(29);
        assertThat(response.getStatus()).isEqualTo(WARNINGS);
    }

    @Test
    void shouldReturnWarningsIfValidateYourApplicationD8ValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicationForDivorce"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "aSoleApplication"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "marriageOrCivilPartnershipCertificate"));
    }

    @Test
    void shouldReturnWarningsIfDuplicateValuesPassedToValidateYourApplication() {

        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(
                new OcrDataField("aSoleApplication", "true"),
                new OcrDataField("aJointApplication", "true"),
                new OcrDataField("marriageOrCivilPartnershipCertificate", "true"),
                new OcrDataField("translation", "true"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELDS_CANNOT_MATCH, "aSoleApplication", "aJointApplication"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELDS_CANNOT_MATCH, "marriageOrCivilPartnershipCertificate", "translation"));
    }

    @Test
    void shouldReturnWarningsIfValidateYourApplicationD8SValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8S.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "aSoleApplication"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "marriageOrCivilPartnershipCertificate"));
    }

    @Test
    void shouldReturnWarningsIfValidateAboutYouValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(new OcrDataField("soleOrApplicant1Solicitor", "Yes")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1FirstName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1LastName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1SolicitorName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1SolicitorFirm"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1SolicitorBuildingAndStreet"));
    }

    @Test
    void shouldReturnWarningsIfValidateAboutTheRespondentValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("aSoleApplication", "true"),
                    new OcrDataField("respondentOrApplicant2MarriedName", "Yes"),
                    new OcrDataField("respondentOrApplicant2Email", "test@email.com"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2FirstName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2LastName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2WhyMarriedNameChanged"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "respondentEmailAccess"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "serveOutOfUK"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "respondentServePostOnly"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicantWillServeApplication"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "respondentDifferentServiceAddress"));
    }

    @Test
    void shouldReturnWarningsIfValidateDetailsOfUnionValidationFailsNoDate() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("marriageOutsideOfUK", "Yes"),
                    new OcrDataField("makingAnApplicationWithoutCertificate", "Yes"),
                    new OcrDataField("detailsOnCertCorrect", "No"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains("Additional D11 application should be filed with additional fee when applying without certificate");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "placeOfMarriageOrCivilPartnership"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "dateOfMarriageOrCivilPartnershipDay"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "dateOfMarriageOrCivilPartnershipMonth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "dateOfMarriageOrCivilPartnershipYear"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "reasonWhyCertNotCorrect"));
    }

    @Test
    void shouldReturnWarningsIfValidateDetailsOfUnionValidationFailsInvalidDate() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("marriageOutsideOfUK", "Yes"),
                    new OcrDataField("makingAnApplicationWithoutCertificate", "Yes"),
                    new OcrDataField("detailsOnCertCorrect", "No"),
                    new OcrDataField("dateOfMarriageOrCivilPartnershipDay", "01"),
                    new OcrDataField("dateOfMarriageOrCivilPartnershipMonth", "01"),
                    new OcrDataField("dateOfMarriageOrCivilPartnershipYear", "year"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains("Additional D11 application should be filed with additional fee when applying without certificate");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "placeOfMarriageOrCivilPartnership"));
        assertThat(response.getWarnings())
            .contains("dateOfMarriageOrCivilPartnership is not valid");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "reasonWhyCertNotCorrect"));
    }

    @Test
    void shouldReturnWarningsIfValidateJurisdictionValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains("Invalid jurisdiction: jurisdiction connection has not been selected");
    }

    @Test
    void shouldReturnWarningsIfValidateStatementOfIrretrievableBreakdownSoleValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("aSoleApplication", "true"),
                    new OcrDataField("applicant2ConfirmationOfBreakdown", "true"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1ConfirmationOfBreakdown"));
        assertThat(response.getWarnings())
            .contains("applicant2ConfirmationOfBreakdown should not be populated for sole applications");
    }

    @Test
    void shouldReturnWarningsIfValidateStatementOfIrretrievableBreakdownJointValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(new OcrDataField("aJointApplication", "true")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1ConfirmationOfBreakdown"));
        assertThat(response.getWarnings())
            .contains("applicant2ConfirmationOfBreakdown should be populated for joint applications");

    }

    @Test
    void shouldReturnWarningsIfValidateExistingCourtCaseValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(new OcrDataField("existingOrPreviousCourtCases", "Yes")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "existingOrPreviousCourtCaseNumbers"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "summaryOfExistingOrPreviousCourtCases"));
    }

    @Test
    void shouldReturnWarningsIfD8FormValidateMoneyPropertyAndPrayerValidationFailsWithFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("soleOrApplicant1FinancialOrder", "Yes"),
                    new OcrDataField("applicant2FinancialOrder", "Yes"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicant2FinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1PrayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1PrayerFinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2PrayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2PrayerFinancialOrderFor"));
    }

    @Test
    void shouldReturnWarningsIfD8FormValidateMoneyPropertyAndPrayerValidationFailsWithoutFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("soleOrApplicant1FinancialOrder", "No"),
                    new OcrDataField("applicant2FinancialOrder", "No"),
                    new OcrDataField("soleOrApplicant1prayerFinancialOrder", "test"),
                    new OcrDataField("soleOrApplicant1prayerFinancialOrderFor", "test"),
                    new OcrDataField("applicant2PrayerFinancialOrder", "test"),
                    new OcrDataField("applicant2PrayerFinancialOrderFor", "test"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrderFor"));
    }

    @Test
    void shouldReturnWarningsIfD8sFormValidateMoneyPropertyAndPrayerValidationFailsWithFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("soleOrApplicant1FinancialOrder", "Yes"),
                    new OcrDataField("applicant2FinancialOrder", "Yes"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8S.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicant2FinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "prayerApplicant1JudiciallySeparated"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1PrayerFinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2PrayerFinancialOrderFor"));
    }

    @Test
    void shouldReturnWarningsIfD8sFormValidateMoneyPropertyAndPrayerValidationFailsWithoutFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("soleOrApplicant1FinancialOrder", "No"),
                    new OcrDataField("applicant2FinancialOrder", "No"),
                    new OcrDataField("soleOrApplicant1prayerFinancialOrderFor", "test"),
                    new OcrDataField("applicant2PrayerFinancialOrderFor", "test"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8S.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "soleOrApplicant1prayerFinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(WARNING_NOT_APPLYING_FINANCIAL_ORDER, "applicant2PrayerFinancialOrderFor"));
    }

    @Test
    void shouldReturnWarningsIfValidateStatementOfTruthValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("aJointApplication", "true")
                )
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1StatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1LegalRepStatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleApplicantOrApplicant1OrLegalRepSignature"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleApplicantOrApplicant1Signing"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "legalRepSigning"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "statementOfTruthDateDay"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "statementOfTruthDateMonth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "statementOfTruthDateYear"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleApplicantOrApplicant1OrLegalRepFullName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicant2StatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicant2LegalRepStatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2OrLegalRepSignature"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2Signing"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2LegalRepSigning"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2StatementOfTruthDateDay"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2StatementOfTruthDateMonth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2StatementOfTruthDateYear"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2OrLegalRepFullName"));
    }

    @Test
    void shouldValidateHelpWithFeesNumbers() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    new OcrDataField("soleOrApplicant1HWFNo", "HWF1"),
                    new OcrDataField("applicant2HWFNo", "HWF1")
                )
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains("soleOrApplicant1HWFNo should be 6 digits long");
        assertThat(response.getWarnings())
            .contains("applicant2HWFNo should be 6 digits long");
    }
}
