package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.ProgressApplicant2FinalOrderState;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ProgressApplicant2FinalOrderStateTest {
    private final ProgressApplicant2FinalOrderState task = new ProgressApplicant2FinalOrderState();

    @Test
    void shouldSetStateToFinalOrderRequestedIfEnglishSoleApplication() {

        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(AwaitingFinalOrder).build();
        var result = task.apply(details);

        assertEquals(result.getState(), RespondentFinalOrderRequested);
    }

    @Test
    void shouldSetStateToAwaitingJointFinalOrderIfEnglishJointApplicationFirstInTime() {

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(AwaitingFinalOrder).build();
        var result = task.apply(details);

        assertEquals(result.getState(), AwaitingJointFinalOrder);
    }

    @Test
    void shouldSetStateToFinalOrderRequestedIfEnglishJointApplicationSecondInTime() {

        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(AwaitingJointFinalOrder).build();
        var result = task.apply(details);

        assertEquals(result.getState(), FinalOrderRequested);
    }
}
