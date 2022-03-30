package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
public class CorrectRelationshipDetailsTest {

    private final CorrectRelationshipDetails page = new CorrectRelationshipDetails();

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
}
