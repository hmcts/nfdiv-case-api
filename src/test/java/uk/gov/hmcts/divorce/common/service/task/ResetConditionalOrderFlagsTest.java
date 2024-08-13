package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ResetConditionalOrderFlagsTest {
    private ResetConditionalOrderFlags resetConditionalOrderFlags;

    @BeforeEach
    void setUp() {
        resetConditionalOrderFlags = new ResetConditionalOrderFlags();
    }

    @Test
    void resetsConditionalOrderFlagsForSoleCases() {
        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(
                    completedConditionalOrderQuestions()
                ).build()
            ).build();

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .build();

        resetConditionalOrderFlags.apply(caseDetails);

        final ConditionalOrderQuestions app1CoQuestions = caseData.getConditionalOrder().getConditionalOrderApplicant1Questions();

        assertAll(
            () -> assertEquals(YesOrNo.NO, app1CoQuestions.getIsDrafted()),
            () -> assertEquals(YesOrNo.NO, app1CoQuestions.getIsSubmitted())
        );
    }

    @Test
    void resetsConditionalOrderFlagsForJointCases() {
        final CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(
                    completedConditionalOrderQuestions()
                )
                .conditionalOrderApplicant2Questions(
                    completedConditionalOrderQuestions()
                ).build()
            ).build();

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData)
            .build();

        resetConditionalOrderFlags.apply(caseDetails);

        final ConditionalOrderQuestions app1CoQuestions = caseData.getConditionalOrder().getConditionalOrderApplicant1Questions();
        final ConditionalOrderQuestions app2CoQuestions = caseData.getConditionalOrder().getConditionalOrderApplicant2Questions();

        assertAll(
            () -> assertEquals(YesOrNo.NO, app1CoQuestions.getIsDrafted()),
            () -> assertEquals(YesOrNo.NO, app1CoQuestions.getIsSubmitted()),
            () -> assertEquals(YesOrNo.NO, app2CoQuestions.getIsDrafted()),
            () -> assertEquals(YesOrNo.NO, app2CoQuestions.getIsSubmitted())
        );
    }

    private ConditionalOrderQuestions completedConditionalOrderQuestions() {
        return ConditionalOrderQuestions.builder()
            .isDrafted(YesOrNo.YES)
            .isSubmitted(YesOrNo.YES)
            .build();
    }
}
