package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
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
    public void shouldReturnErrorsIfPrayerCheckboxIsNull() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(null);
        caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors())
            .contains("Applicant 1 prayer must not be empty");
        assertThat(response.getErrors())
            .contains("Applicant 2 prayer must not be empty");
    }

    @Test
    public void shouldReturnErrorsIfPrayerCheckboxIsEmpty() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant1PrayerHasBeenGivenCheckbox(emptySet());
        caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(emptySet());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors())
            .contains("Applicant 1 prayer must be yes");
        assertThat(response.getErrors())
            .contains("Applicant 2 prayer must be yes");
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
    public void shouldSetCaseLabelsForSoleDivorce() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getLabelContent().getApplicant2(),
            "respondent"
        );
        assertEquals(
            response.getData().getLabelContent().getUnionType(),
            "divorce"
        );
    }

    @Test
    public void shouldSetCaseLabelsForJointDissolution() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getLabelContent().getApplicant2(),
            "applicant 2"
        );
        assertEquals(
            response.getData().getLabelContent().getUnionType(),
            "dissolution"
        );
    }

    @Test
    public void shouldSetConditionalOrderFlagFieldsForSoleApplication() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted(),
            YesOrNo.NO
        );
        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted(),
            YesOrNo.NO
        );
    }

    @Test
    public void shouldSetConditionalOrderFlagFieldsForJointApplication() {
        final CaseData caseData = validApplicant2CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted(),
            YesOrNo.NO
        );
        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted(),
            YesOrNo.NO
        );
        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted(),
            YesOrNo.NO
        );
        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted(),
            YesOrNo.NO
        );
    }
}
