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
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.FIELD_EMPTY_OR_MISSING;
import static uk.gov.hmcts.divorce.bulkscan.validation.OcrValidator.WARNING_NOT_APPLYING_FINANCIAL_ORDER;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.SUCCESS;
import static uk.gov.hmcts.divorce.endpoint.data.ValidationStatus.WARNINGS;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.populateKeyValue;
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

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getErrors()).hasSize(0);
        assertThat(response.getWarnings()).hasSize(0);
        assertThat(response.getStatus()).isEqualTo(SUCCESS);
    }

    @Test
    void shouldReturnWarningsWhenNoOcrDataPassed() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getErrors()).hasSize(0);
        assertThat(response.getWarnings()).hasSize(28);
        assertThat(response.getStatus()).isEqualTo(WARNINGS);
    }

    @Test
    void shouldReturnWarningsIfValidateYourApplicationValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicationForDivorce"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "aSoleApplication"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "marriageOrCivilPartnershipCertificate"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "translation"));
    }

    @Test
    void shouldReturnWarningsIfValidateAboutYouSolicitorValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(populateKeyValue("soleOrApplicant1Solicitor", "Yes")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1SolicitorName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1SolicitorFirm"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1SolicitorBuildingAndStreet"));
    }

    @Test
    void shouldReturnWarningsIfValidateAboutTheRespondentValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("aSoleApplication", "true"),
                    populateKeyValue("respondentOrApplicant2MarriedName", "Yes"),
                    populateKeyValue("respondentOrApplicant2Email", "test@email.com"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "respondentOrApplicant2WhyMarriedNameChanged"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "respondentEmailAccess"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "serveOutOfUK"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "respondentServePostOnly"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicantWillServeApplication"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "respondentDifferentServiceAddress"));
    }

    @Test
    void shouldReturnWarningsIfValidateDetailsOfUnionValidationFailsNoDate() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("marriageOutsideOfUK", "Yes"),
                    populateKeyValue("makingAnApplicationWithoutCertificate", "Yes"),
                    populateKeyValue("detailsOnCertCorrect", "No"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains("Additional D11 application should be filed with additional fee when applying without certificate");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "placeOfMarriageOrCivilPartnership"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "dateOfMarriageOrCivilPartnershipDay"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "dateOfMarriageOrCivilPartnershipMonth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "dateOfMarriageOrCivilPartnershipYear"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "respondentOrApplicant2FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "reasonWhyCertNotCorrect"));
    }

    @Test
    void shouldReturnWarningsIfValidateDetailsOfUnionValidationFailsInvalidDate() {
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

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains("Additional D11 application should be filed with additional fee when applying without certificate");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "placeOfMarriageOrCivilPartnership"));
        assertThat(response.getWarnings())
            .contains("dateOfMarriageOrCivilPartnership is not valid");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "respondentOrApplicant2FullNameAsOnCert"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "reasonWhyCertNotCorrect"));
    }

    @Test
    void shouldReturnWarningsIfValidateJurisdictionValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(emptyList())
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
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

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1ConfirmationOfBreakdown"));
        assertThat(response.getWarnings())
            .contains("applicant2ConfirmationOfBreakdown should not be populated for sole applications");
    }

    @Test
    void shouldReturnWarningsIfValidateStatementOfIrretrievableBreakdownJointValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(populateKeyValue("aJointApplication", "true")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1ConfirmationOfBreakdown"));
        assertThat(response.getWarnings())
            .contains("applicant2ConfirmationOfBreakdown should be populated for joint applications");

    }

    @Test
    void shouldReturnWarningsIfValidateExistingCourtCaseValidationFails() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(List.of(populateKeyValue("existingOrPreviousCourtCases", "Yes")))
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "existingOrPreviousCourtCaseNumbers"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "summaryOfExistingOrPreviousCourtCases"));
    }

    @Test
    void shouldReturnWarningsIfValidateMoneyPropertyAndPrayerValidationFailsWithFinancialOrder() {
        final OcrDataValidationRequest request = OcrDataValidationRequest.builder()
            .ocrDataFields(
                List.of(
                    populateKeyValue("soleOrApplicant1FinancialOrder", "Yes"),
                    populateKeyValue("applicant2FinancialOrder", "Yes"))
            )
            .build();

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1FinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2FinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains("One of prayerMarriageDissolved or prayerCivilPartnershipDissolved must be populated");
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1prayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleOrApplicant1prayerFinancialOrderFor"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2PrayerFinancialOrder"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2PrayerFinancialOrderFor"));
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

        OcrValidationResponse response = validator.validateExceptionRecord(request);

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

        OcrValidationResponse response = validator.validateExceptionRecord(request);

        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleApplicantOrApplicant1StatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleApplicantOrApplicant1LegalRepStatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleApplicantOrApplicant1OrLegalRepSignature"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleApplicantOrApplicant1Signing"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "legalRepSigning"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "statementOfTruthDateDay"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "statementOfTruthDateMonth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "statementOfTruthDateYear"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "soleApplicantOrApplicant1OrLegalRepFullName"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2StatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2LegalRepStatementOfTruth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2OrLegalRepSignature"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2Signing"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2LegalRepSigning"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2StatementOfTruthDateDay"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2StatementOfTruthDateMonth"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2StatementOfTruthDateYear"));
        assertThat(response.getWarnings())
            .contains(String.format(FIELD_EMPTY_OR_MISSING, "applicant2OrLegalRepFullName"));
        assertThat(response.getWarnings())
            .contains("soleOrApplicant1HWFNo should be 6 digits long");
    }
}
