package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class HandleWelshFinalOrderTest {
    private final HandleWelshFinalOrder task = new HandleWelshFinalOrder();

    @Test
    void shouldNotChangeDataIfApplicationIsNotWelsh() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant1().setUsedWelshTranslationOnSubmission(NO);
        caseData.getApplicant2().setUsedWelshTranslationOnSubmission(NO);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1FinalOrderLateExplanation("Test content applicant1FinalOrderLateExplanation")
            .applicant2FinalOrderLateExplanation("Test content applicant2FinalOrderLateExplanation")
            .build());

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(FinalOrderRequested).build();
        var result = task.apply(details);

        assertEquals(details, result);
    }

    @Test
    void shouldNotChangeDetailsIfApplicationIsOnTimeAndThereforeHasNoDelayReason() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant1().setUsedWelshTranslationOnSubmission(NO);
        caseData.getApplicant2().setUsedWelshTranslationOnSubmission(NO);
        caseData.setFinalOrder(FinalOrder.builder().build());

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(FinalOrderRequested).build();
        var result = task.apply(details);

        assertEquals(details, result);
    }

    @Test
    void shouldNotChangeDetailsIfApplicationHasNotMovedToFinalOrderRequestedYet() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant1().setUsedWelshTranslationOnSubmission(NO);
        caseData.getApplicant2().setUsedWelshTranslationOnSubmission(NO);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1FinalOrderLateExplanation("Test content applicant1FinalOrderLateExplanation")
            .applicant2FinalOrderLateExplanation("Test content applicant2FinalOrderLateExplanation")
            .build());

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(AwaitingJointFinalOrder).build();
        var result = task.apply(details);

        assertEquals(details, result);
    }

    @Test
    void shouldSetWelshPreviousStateAndStateWhenWelshOverdueAndBothApplicantsHaveRequestedFinalOrder() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.getApplicant1().setUsedWelshTranslationOnSubmission(NO);
        caseData.getApplicant2().setUsedWelshTranslationOnSubmission(NO);
        caseData.setFinalOrder(FinalOrder.builder()
            .applicant1FinalOrderLateExplanation("Test content applicant1FinalOrderLateExplanation")
            .applicant2FinalOrderLateExplanation("Test content applicant2FinalOrderLateExplanation")
            .build());

        var details = CaseDetails.<CaseData, State>builder().data(caseData).state(FinalOrderRequested).build();
        var result = task.apply(details);

        var expectedDetails = CaseDetails.<CaseData, State>builder().data(caseData).state(WelshTranslationReview).build();
        expectedDetails.getData().getApplication().setWelshPreviousState(FinalOrderRequested);

        assertEquals(expectedDetails, result);
    }
}
