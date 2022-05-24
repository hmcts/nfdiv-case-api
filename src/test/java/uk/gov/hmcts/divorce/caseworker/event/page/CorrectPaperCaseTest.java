package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CivilPartnershipBroken;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageBroken;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CivilPartnershipBroken.CIVIL_PARTNERSHIP_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageBroken.MARRIAGE_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.SOT_REQUIRED;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class CorrectPaperCaseTest {

    private final CorrectPaperCase page = new CorrectPaperCase();

    @Test
    public void shouldSuccessfullyValidateValuesPassedByUser() {
        final CaseData caseData = validApplicant2CaseData();

        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void shouldSuccessfullyValidateValuesPassedByUserDissolution() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);

        Set<CivilPartnershipBroken> civilPartnershipBroken = new HashSet<>();
        civilPartnershipBroken.add(CIVIL_PARTNERSHIP_BROKEN);
        caseData.getApplication().setApplicant1HasCivilPartnershipBroken(civilPartnershipBroken);

        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    public void shouldReturnErrorsIfScreenHasMarriageBrokenIsNo() {
        final CaseData caseData = validApplicant2CaseData();

        Set<MarriageBroken> marriageBroken = new HashSet<>();
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

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
    public void shouldReturnErrorsIfScreenHasMarriageBrokenIsNull() {
        final CaseData caseData = validApplicant2CaseData();

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
    public void shouldReturnErrorsIfScreenHasCivilPartnershipBrokenIsNo() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);

        Set<CivilPartnershipBroken> civilPartnershipBroken = new HashSet<>();
        caseData.getApplication().setApplicant1HasCivilPartnershipBroken(civilPartnershipBroken);

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
    public void shouldReturnErrorsIfScreenHasCivilPartnershipBrokenIsNull() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);

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

        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

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

        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

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

        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

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

        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

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
}
