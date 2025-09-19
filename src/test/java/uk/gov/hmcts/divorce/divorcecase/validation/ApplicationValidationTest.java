package uk.gov.hmcts.divorce.divorcecase.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.cloud.contract.verifier.assertion.SpringCloudContractAssertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.SERVICE_DOCUMENTS_ALREADY_REGENERATED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class ApplicationValidationTest {

    @Test
    void shouldReturnValidationErrorsIfRepeatedServiceTooRecently() {
        final var caseData = TestDataHelper.caseData();
        caseData.getApplication().setServiceDocumentsRegeneratedDate(
            LocalDate.now().minusDays(1)
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final List<String> result = ApplicationValidation.validateServiceDate(caseData, 14);

        assertThat(result).containsExactly(SERVICE_DOCUMENTS_ALREADY_REGENERATED);
    }

    @Test
    void shouldNotReturnValidationErrorsIfRepeatedServiceFarInThePast() {
        final var caseData = TestDataHelper.caseData();
        caseData.getApplication().setServiceDocumentsRegeneratedDate(
            LocalDate.now().minusDays(30)
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final List<String> result = ApplicationValidation.validateServiceDate(caseData, 14);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldNotReturnValidationErrorsIfHasNeverRepeatedService() {
        final var caseData = TestDataHelper.caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        final List<String> result = ApplicationValidation.validateServiceDate(caseData, 14);

        assertThat(result).isEmpty();
    }
}
