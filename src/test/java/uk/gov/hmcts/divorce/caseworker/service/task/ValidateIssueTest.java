package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.exception.InvalidDataException;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation;

import java.util.Collections;

import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_VALIDATION_ERROR;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ValidateIssueTest {

    @InjectMocks
    private ValidateIssue validateIssue;

    @Test
    void shouldThrowExceptionWhenThereAreValidationErrors() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateIssue(caseDetails.getData()))
                .thenReturn(Collections.singletonList(TEST_VALIDATION_ERROR));

            InvalidDataException thrown = assertThrows(InvalidDataException.class, () -> {
                validateIssue.apply(caseDetails);
            });

            assert(thrown.getErrors().contains(TEST_VALIDATION_ERROR));
        }
    }

    @Test
    void shouldNotThrowExceptionWhenThereAreNoValidationErrors() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        try (MockedStatic<ApplicationValidation> classMock = Mockito.mockStatic(ApplicationValidation.class)) {
            classMock.when(() -> ApplicationValidation.validateIssue(caseDetails.getData()))
                .thenReturn(Collections.emptyList());

            assert validateIssue.apply(caseDetails).equals(caseDetails);
        }
    }
}
