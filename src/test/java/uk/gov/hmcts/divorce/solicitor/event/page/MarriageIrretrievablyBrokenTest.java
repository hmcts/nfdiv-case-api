package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class MarriageIrretrievablyBrokenTest {

    private final MarriageIrretrievablyBroken page = new MarriageIrretrievablyBroken();

    @Test
    void shouldPreventProgressIfMarriageNotBroken() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(1, response.getErrors().size());
        assertEquals(
            "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken",
            response.getErrors().get(0)
        );
    }

    @Test
    void shouldPreventProgressIfMarriageNotBrokenForBothApplicants() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(NO);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(2, response.getErrors().size());
        assertEquals(
            "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken",
            response.getErrors().get(0)
        );
        assertEquals(
            "To continue, applicant 2 must believe and declare that their marriage has irrevocably broken",
            response.getErrors().get(1)
        );
    }

    @Test
    void shouldAllowProgressIfMarriageIsBroken() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldAllowProgressIfMarriageIsBrokenForBothApplicants() {
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1ScreenHasMarriageBroken(YES);
        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(0, response.getErrors().size());
    }
}
