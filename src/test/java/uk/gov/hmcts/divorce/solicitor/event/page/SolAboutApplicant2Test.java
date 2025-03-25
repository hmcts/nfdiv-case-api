package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class SolAboutApplicant2Test {

    private final SolAboutApplicant2 page = new SolAboutApplicant2();

    @Test
    public void shouldCallValidationUtilMethodToValidateApplicant2Names() {
        final CaseData caseData = validApplicant2CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        List<String> errors = new ArrayList<>();
        errors.add("Error");

        MockedStatic<ValidationUtil> validationUtilMockedStatic = Mockito.mockStatic(ValidationUtil.class);
        validationUtilMockedStatic.when(() -> ValidationUtil.validateApplicant2NameForAllowedCharacters(caseData)).thenReturn(errors);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Error");

        validationUtilMockedStatic.close();
    }

    @Test
    public void shouldNotReturnAnyErrorsWhenValidationIsSuccessful() {
        final CaseData caseData = validApplicant2CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        List<String> errors = Collections.emptyList();

        MockedStatic<ValidationUtil> validationUtilMockedStatic = Mockito.mockStatic(ValidationUtil.class);
        validationUtilMockedStatic.when(() -> ValidationUtil.validateApplicant2NameForAllowedCharacters(caseData)).thenReturn(errors);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNull();

        validationUtilMockedStatic.close();
    }
}
