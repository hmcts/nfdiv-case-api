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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SolAboutApplicant1Test {

    private final SolAboutApplicant1 page = new SolAboutApplicant1();

    @Test
    public void shouldReturnErrorIfEmailValidationFails() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail("invalidEmail");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 1);
        assertThat(response.getErrors()).containsExactly("You have entered an invalid email address. "
            + "Please check the email and enter it again, before submitting the application.");
    }

    @Test
    public void shouldNotReturnErrorIfEmailValidationPasses() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertNull(response.getErrors());
    }

    @Test
    public void shouldCallValidationUtilMethodToValidateApplicant1Names() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName("F!rstName");
        caseData.getApplicant1().setMiddleName("M1ddleName");
        caseData.getApplicant1().setLastName("La$tName");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        List<String> errors = new ArrayList<>();
        errors.add("Error");

        MockedStatic<ValidationUtil> validationUtilMockedStatic = Mockito.mockStatic(ValidationUtil.class);
        validationUtilMockedStatic.when(() -> ValidationUtil.validateApplicant1NameForAllowedCharacters(caseData)).thenReturn(errors);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Error");

        validationUtilMockedStatic.close();
    }
}
