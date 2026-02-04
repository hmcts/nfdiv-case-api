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
    void shouldSetContentForSoleDivorces() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicationType(SOLE_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            "respondent",
            response.getData().getLabelContent().getApplicant2()
        );
        assertEquals(
            "divorce",
            response.getData().getLabelContent().getUnionType()
        );
    }

    @Test
    void shouldSetContentForJointDissolutions() {
        final CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            "applicant 2",
            response.getData().getLabelContent().getApplicant2()
        );
        assertEquals(
            "dissolution",
            response.getData().getLabelContent().getUnionType()
        );
    }

    @Test
    void shouldSetCoIsSubmittedForSole() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder().build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            YesOrNo.NO,
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()
        );
    }

    @Test
    void shouldSetCoIsSubmittedForJoint() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            YesOrNo.NO,
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()
        );
        assertEquals(
            YesOrNo.NO,
            response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()
        );
    }

    @Test
    void shouldSetCoIsDraftedForSole() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder().build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            YesOrNo.NO,
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()
        );
    }

    @Test
    void shouldSetCoIsDraftedForJoint() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(
            YesOrNo.NO,
            response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()
        );
        assertEquals(
            YesOrNo.NO,
            response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()
        );
    }
}
