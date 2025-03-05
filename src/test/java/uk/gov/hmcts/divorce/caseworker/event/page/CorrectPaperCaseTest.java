package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.SOT_REQUIRED;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class CorrectPaperCaseTest {

    private final CorrectPaperCase page = new CorrectPaperCase();

    @Test
    public void shouldSuccessfullyValidateValuesPassedByUser() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void shouldReturnErrorsIfScreenHasMarriageBrokenIsNo() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors())
            .contains("To continue, applicant 1 must believe and declare that their marriage has irrevocably broken");
        assertThat(response.getErrors())
            .contains("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
    }

    @Test
    public void shouldReturnErrorsIfStatementOfTruthNotAcceptedForSoleApplication() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant1StatementOfTruth(NO);
        caseData.getApplication().setApplicant1StatementOfTruth(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors())
            .contains(SOT_REQUIRED);
    }

    @Test
    public void shouldReturnErrorsIfStatementOfTruthNotAccepted() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant1StatementOfTruth(NO);
        caseData.getApplication().setApplicant2StatementOfTruth(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors())
            .contains(SOT_REQUIRED);
        assertThat(response.getErrors())
            .contains("Statement of truth must be accepted by Applicant 2 for joint applications");
    }

    @Test
    public void shouldNotReturnErrorsIfMarriageBrokenNotAcceptedByApplicant2ForSoleApplication() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant2StatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void shouldReturnErrorsIfMarriageBrokenNotAcceptedByApplicant2ForJointApplication() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant2StatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors())
            .contains("To continue, applicant 2 must believe and declare that their marriage has irrevocably broken");
    }

    @Test
    public void shouldReturnErrorsIfNamesContainInvalidCharacters() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().getMarriageDetails().setApplicant1Name("Inva(id App1Name");
        caseData.getApplication().getMarriageDetails().setApplicant2Name("Inva(id App2Name");
        caseData.getApplicant1().setFirstName("Inva(id");
        caseData.getApplicant1().setMiddleName("Inva1id");
        caseData.getApplicant1().setLastName("Inva$id");
        caseData.getApplicant2().setFirstName("Inva(id");
        caseData.getApplicant2().setMiddleName("Inva1id");
        caseData.getApplicant2().setLastName("Inva$id");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .containsExactlyInAnyOrder(
                "Applicant or Applicant 1 first name has invalid characters",
                "Applicant or Applicant 1 middle name has invalid characters",
                "Applicant or Applicant 1 last name has invalid characters",
                "Respondent or Applicant 2 first name has invalid characters",
                "Respondent or Applicant 2 middle name has invalid characters",
                "Respondent or Applicant 2 last name has invalid characters",
                "Applicant or Applicant 1 name on marriage certificate has invalid characters",
                "Respondent or Applicant 2 name on marriage certificate has invalid characters"
            );
    }

    @Test
    public void shouldNotReturnErrorsIfNamesContainValidCharacters() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().setApplicant2StatementOfTruth(YES);
        caseData.getApplication().getMarriageDetails().setApplicant1Name("Valid app_licant-namé");
        caseData.getApplication().getMarriageDetails().setApplicant2Name("Valid respondent-namé");
        caseData.getApplicant1().setFirstName("Valid");
        caseData.getApplicant1().setLastName("Valid");
        caseData.getApplicant2().setFirstName("Valid");
        caseData.getApplicant2().setLastName("Valid");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }
}
