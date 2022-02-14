package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolHowDoYouWantToApplyForDivorceTest {

    private final SolHowDoYouWantToApplyForDivorce page = new SolHowDoYouWantToApplyForDivorce();

    @Test
    public void shouldSetContentForSoleDivorces() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);

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
    public void shouldSetContentForJointDissolutions() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(JOINT_APPLICATION);

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
    public void shouldSetCoIsSubmittedForSole() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder().build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted(),
            YesOrNo.NO
        );
    }

    @Test
    public void shouldSetCoIsSubmittedForJoint() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted(),
            YesOrNo.NO
        );
        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted(),
            YesOrNo.NO
        );
    }

    @Test
    public void shouldSetCoIsDraftedForSole() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder().build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted(),
            YesOrNo.NO
        );
    }

    @Test
    public void shouldSetCoIsDraftedForJoint() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);

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
    }
}
