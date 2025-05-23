package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderReviewAoSTest {

    private final ConditionalOrderReviewAoS page = new ConditionalOrderReviewAoS();

    private static final String APPLY_FOR_CONDITIONAL_ORDER_NO_ERROR_APP1 = "Applicant must select yes to apply for a conditional order";

    @Test
    void shouldPreventProgressIfNoIsSelectedForSoleApplicationOnApplyForConditionalOrderQuestion() {

        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        ConditionalOrderQuestions conditionalOrderQuestions = new ConditionalOrderQuestions();
        conditionalOrderQuestions.setApplyForConditionalOrder(NO);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(conditionalOrderQuestions)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(1, response.getErrors().size());
        assertEquals(
            APPLY_FOR_CONDITIONAL_ORDER_NO_ERROR_APP1,
            response.getErrors().get(0)
        );
    }

    @Test
    void shouldNotPreventProgressIfNoIsSelectedForJointApplicationOnApplyForConditionalOrderQuestion() {

        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        ConditionalOrderQuestions conditionalOrderQuestions = new ConditionalOrderQuestions();
        conditionalOrderQuestions.setApplyForConditionalOrder(NO);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(conditionalOrderQuestions)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldAllowProgressIfYesIsSelectedOnApplyForConditionalOrderQuestion() {

        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        ConditionalOrderQuestions conditionalOrderQuestions = new ConditionalOrderQuestions();
        conditionalOrderQuestions.setApplyForConditionalOrder(YES);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(conditionalOrderQuestions)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(0, response.getErrors().size());
    }
}
