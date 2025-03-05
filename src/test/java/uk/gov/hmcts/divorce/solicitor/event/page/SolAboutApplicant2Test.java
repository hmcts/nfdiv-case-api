package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class SolAboutApplicant2Test {

    private final SolAboutApplicant2 page = new SolAboutApplicant2();

    @Test
    public void shouldReturnErrorIfApplicant2NameHasInvalidCharacters() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplicant2().setFirstName("F!rstName");
        caseData.getApplicant2().setMiddleName("M1ddleName");
        caseData.getApplicant2().setLastName("La$tName");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);
        assertEquals(response.getErrors().size(), 3);
        assertThat(response.getErrors())
            .containsExactlyInAnyOrder(
                "Respondent or Applicant 2 first name has invalid characters",
                "Respondent or Applicant 2 middle name has invalid characters",
                "Respondent or Applicant 2 last name has invalid characters"
            );
    }

    @Test
    public void shouldNotReturnErrorIfApplicant2NameIsValid() {
        final CaseData caseData = validApplicant2CaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);
        assertNull(response.getErrors());
    }
}
