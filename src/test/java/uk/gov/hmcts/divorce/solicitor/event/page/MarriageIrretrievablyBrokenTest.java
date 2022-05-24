package uk.gov.hmcts.divorce.solicitor.event.page;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.CivilPartnershipBroken.CIVIL_PARTNERSHIP_BROKEN;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageBroken.MARRIAGE_BROKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class MarriageIrretrievablyBrokenTest {

    private final MarriageIrretrievablyBroken page = new MarriageIrretrievablyBroken();

    @Test
    public void shouldPreventProgressIfMarriageNotBroken() {
        final CaseData caseData = caseData();
        Set<MarriageBroken> marriageBroken = new HashSet<>();
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 1);
        assertEquals(
            response.getErrors().get(0),
            "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken"
        );
    }

    @Test
    public void shouldPreventProgressIfCivilPartnershipNotBroken() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        Set<CivilPartnershipBroken> civilPartnershipBroken = new HashSet<>();
        caseData.getApplication().setApplicant1HasCivilPartnershipBroken(civilPartnershipBroken);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 1);
        assertEquals(
            response.getErrors().get(0),
            "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken"
        );
    }

    @Test
    public void shouldPreventProgressIfMarriageNotBrokenForBothApplicants() {
        final CaseData caseData = caseData();
        Set<MarriageBroken> marriageBroken = new HashSet<>();
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 2);
        assertEquals(
            response.getErrors().get(0),
            "To continue, applicant 1 must believe and declare that their marriage has irrevocably broken"
        );
        assertEquals(
            response.getErrors().get(1),
            "To continue, applicant 2 must believe and declare that their marriage has irrevocably broken"
        );
    }

    @Test
    public void shouldAllowProgressIfMarriageIsBroken() {
        final CaseData caseData = caseData();
        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 0);
    }

    @Test
    public void shouldAllowProgressIfCivilPartnershipIsBroken() {
        final CaseData caseData = caseData();
        Set<CivilPartnershipBroken> civilPartnershipBroken = new HashSet<>();
        civilPartnershipBroken.add(CIVIL_PARTNERSHIP_BROKEN);
        caseData.getApplication().setApplicant1HasCivilPartnershipBroken(civilPartnershipBroken);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 0);
    }

    @Test
    public void shouldAllowProgressIfMarriageIsBrokenForBothApplicants() {
        final CaseData caseData = caseData();
        Set<MarriageBroken> marriageBroken = new HashSet<>();
        marriageBroken.add(MARRIAGE_BROKEN);
        caseData.getApplication().setApplicant1HasMarriageBroken(marriageBroken);

        caseData.getApplication().setApplicant2ScreenHasMarriageBroken(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(response.getErrors().size(), 0);
    }
}
