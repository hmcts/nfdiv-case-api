package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class ValidateIssueTest {

    @InjectMocks
    private ValidateIssue validateIssue;

    @Test
    void shouldReturnValidationErrorsWhenCaseDataIsInvalid() {
        final CaseData caseData = invalidCaseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        List<String> validationErrors = validateIssue.validate(caseDetails);

        assertThat(validationErrors).isNotEmpty();
    }

    @Test
    void shouldReturnSameValidationResultAsApplicationValidation() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        List<String> expected = ApplicationValidation.validateIssue(caseData);
        List<String> actual = validateIssue.validate(caseDetails);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoValidationErrors() {
        final CaseData caseData = validCaseDataForIssueApplication();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        List<String> validationErrors = validateIssue.validate(caseDetails);

        assertThat(validationErrors).isEmpty();
    }
}
