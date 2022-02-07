package uk.gov.hmcts.divorce.bulkscan.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.endpoint.data.OcrDataValidationRequest;
import uk.gov.hmcts.divorce.endpoint.data.OcrValidationResponse;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELDS_CANNOT_MATCH;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELD_EMPTY_OR_MISSING_ERROR;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELD_EMPTY_OR_MISSING_WARNING;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.WARNING_NOT_APPLYING_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8;
import static uk.gov.hmcts.divorce.endpoint.data.FormType.D8S;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.ERRORS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateD8OcrDataFields;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateKeyValue;

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
    void shouldReturnErrorsAndWarningsWhenNoOcrDataPassed() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors()).hasSize(23);
        assertThat(response.getWarnings()).hasSize(7);
        assertThat(response.getStatus()).isEqualTo(ERRORS);
    }

    @Test
    void shouldReturnErrorsIfValidateYourApplicationD8ValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicationForDivorce"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "aSoleApplication"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "marriageOrCivilPartnershipCertificate"));
    }

    @Test
    void shouldReturnErrorsIfDuplicateValuesPassedToValidateYourApplication() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(
                populateKeyValue("aSoleApplication", "true"),
                populateKeyValue("aJointApplication", "true"),
                populateKeyValue("marriageOrCivilPartnershipCertificate", "true"),
                populateKeyValue("translation", "true"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELDS_CANNOT_MATCH, "aSoleApplication", "aJointApplication"));
        assertThat(response.getErrors())
            .contains(String.format(FIELDS_CANNOT_MATCH, "marriageOrCivilPartnershipCertificate", "translation"));
    }

    @Test
    void shouldReturnErrorsIfValidateYourApplicationD8SValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8S.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "aSoleApplication"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "marriageOrCivilPartnershipCertificate"));
    }

    @Test
    void shouldReturnErrorsAndWarningsIfValidateAboutYouValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(populateKeyValue("soleOrApplicant1Solicitor", "Yes")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1FirstName"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1LastName"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1SolicitorName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1SolicitorFirm"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1SolicitorBuildingAndStreet"));
    }

    @Test
    void shouldReturnErrorsAndWarningsIfValidateAboutTheRespondentValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("aSoleApplication", "true"),
                    populateKeyValue("respondentOrApplicant2MarriedName", "Yes"),
                    populateKeyValue("respondentOrApplicant2Email", "test@email.com"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2FirstName"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2LastName"));
        assertThat(response.getErrors())
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
    void shouldReturnErrorsAndWarningsIfValidateDetailsOfUnionValidationFailsNoDate() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("marriageOutsideOfUK", "Yes"),
                    populateKeyValue("makingAnApplicationWithoutCertificate", "Yes"),
                    populateKeyValue("detailsOnCertCorrect", "No"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains("Additional D11 application should be filed with additional fee when applying without certificate");
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "placeOfMarriageOrCivilPartnership"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "dateOfMarriageOrCivilPartnershipDay"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "dateOfMarriageOrCivilPartnershipMonth"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "dateOfMarriageOrCivilPartnershipYear"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FullNameAsOnCert"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2FullNameAsOnCert"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "reasonWhyCertNotCorrect"));
    }

    @Test
    void shouldReturnErrorsAndWarningsIfValidateDetailsOfUnionValidationFailsInvalidDate() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("marriageOutsideOfUK", "Yes"),
                    populateKeyValue("makingAnApplicationWithoutCertificate", "Yes"),
                    populateKeyValue("detailsOnCertCorrect", "No"),
                    populateKeyValue("dateOfMarriageOrCivilPartnershipDay", "01"),
                    populateKeyValue("dateOfMarriageOrCivilPartnershipMonth", "01"),
                    populateKeyValue("dateOfMarriageOrCivilPartnershipYear", "year"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getWarnings())
            .contains("Additional D11 application should be filed with additional fee when applying without certificate");
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "placeOfMarriageOrCivilPartnership"));
        assertThat(response.getErrors())
            .contains("dateOfMarriageOrCivilPartnership is not valid");
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FullNameAsOnCert"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "respondentOrApplicant2FullNameAsOnCert"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "reasonWhyCertNotCorrect"));
    }

    @Test
    void shouldReturnErrorsIfValidateJurisdictionValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains("Invalid jurisdiction: jurisdiction connection has not been selected");
    }

    @Test
    void shouldReturnWarningsIfValidateStatementOfIrretrievableBreakdownSoleValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("aSoleApplication", "true"),
                    populateKeyValue("applicant2ConfirmationOfBreakdown", "true"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1ConfirmationOfBreakdown"));
        assertThat(response.getErrors())
            .contains("applicant2ConfirmationOfBreakdown should not be populated for sole applications");
    }

    @Test
    void shouldReturnErrorsIfValidateStatementOfIrretrievableBreakdownJointValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(populateKeyValue("aJointApplication", "true")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1ConfirmationOfBreakdown"));
        assertThat(response.getErrors())
            .contains("applicant2ConfirmationOfBreakdown should be populated for joint applications");

    }

    @Test
    void shouldReturnErrorsIfValidateExistingCourtCaseValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(populateKeyValue("existingOrPreviousCourtCases", "Yes")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "existingOrPreviousCourtCaseNumbers"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "summaryOfExistingOrPreviousCourtCases"));
    }

    @Test
    void shouldReturnErrorsAndWarningsIfValidateMoneyPropertyAndPrayerValidationFailsWithFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("soleOrApplicant1FinancialOrder", "Yes"),
                    populateKeyValue("applicant2FinancialOrder", "Yes"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleOrApplicant1FinancialOrderFor"));
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicant2FinancialOrderFor"));
        assertThat(response.getErrors())
            .contains("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1prayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "soleOrApplicant1prayerFinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2PrayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_WARNING, "applicant2PrayerFinancialOrderFor"));
    }

    @Test
    void shouldReturnWarningsIfValidateMoneyPropertyAndPrayerValidationFailsWithoutFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("soleOrApplicant1FinancialOrder", "No"),
                    populateKeyValue("applicant2FinancialOrder", "Yes"),
                    populateKeyValue("soleOrApplicant1prayerFinancialOrder", "test"),
                    populateKeyValue("soleOrApplicant1prayerFinancialOrderFor", "test"),
                    populateKeyValue("applicant2PrayerFinancialOrder", "test"),
                    populateKeyValue("applicant2PrayerFinancialOrderFor", "test"))
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
    void shouldReturnWarningsIfValidateStatementOfTruthValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("aJointApplication", "true"),
                    populateKeyValue("soleOrApplicant1HWFNo", "HWF1")
                )
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(D8.getName(), request);

        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "soleApplicantOrApplicant1StatementOfTruth"));
        assertThat(response.getErrors())
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
        assertThat(response.getErrors())
            .contains(String.format(FIELD_EMPTY_OR_MISSING_ERROR, "applicant2StatementOfTruth"));
        assertThat(response.getErrors())
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
        assertThat(response.getErrors())
            .contains("soleOrApplicant1HWFNo should be 6 digits long");
    }
}
