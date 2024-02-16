package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

class Applicant2SolApplyForFinalOrderDetailsTest {

    private final Applicant2SolApplyForFinalOrderDetails page = new Applicant2SolApplyForFinalOrderDetails();

    @Test
    void shouldReturnErrorWhenApplyForFinalOrderIsNoAndMidEventIsInvoked() {
        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicant2WantToApplyForFinalOrder(NO);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).containsExactly("You must select 'Yes' to apply for Final Order");
    }

    @Test
    void shouldNotReturnErrorWhenApplyForFinalOrderIsYesAndMidEventIsInvoked() {
        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicant2WantToApplyForFinalOrder(YES);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, beforeDetails);

        assertThat(response.getErrors()).isNull();
    }
}
