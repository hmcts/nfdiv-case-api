package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorSwitchToSoleFo.APPLICANT_1_SOLICITOR_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class Applicant1SolicitorSwitchToSoleFoTest {

    @InjectMocks
    private Applicant1SolicitorSwitchToSoleFo applicant1SolicitorSwitchToSoleFo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1SolicitorSwitchToSoleFo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_1_SOLICITOR_SWITCH_TO_SOLE_FO);
    }

    @Test
    void shouldReturnErrorOnMidEventIfNoIsSelectedOnDoesApplicant1WantToApplyForFinalOrder() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1WantToApplyForFinalOrder(NO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant1SolicitorSwitchToSoleFo.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("You must select 'Yes' to apply for Final Order");

    }

    @Test
    void shouldNotReturnErrorsOnMidEventIfYesIsSelectedOnDoesApplicant1WantToApplyForFinalOrder() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1WantToApplyForFinalOrder(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant1SolicitorSwitchToSoleFo.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldClearFieldDataInAboutToStartCallback() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1WantToApplyForFinalOrder(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant1SolicitorSwitchToSoleFo.aboutToStart(caseDetails);

        assertThat(response.getData().getFinalOrder().getDoesApplicant1WantToApplyForFinalOrder()).isNull();
    }

    @Test
    void shouldPopulateSwitchToSoleFinalOrderFields() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1WantToApplyForFinalOrder(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant1SolicitorSwitchToSoleFo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getFinalOrder().getFinalOrderSwitchedToSole()).isEqualTo(YES);
    }
}
