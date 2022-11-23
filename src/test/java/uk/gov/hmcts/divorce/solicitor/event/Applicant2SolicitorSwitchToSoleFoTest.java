package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorSwitchToSoleFo.APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class Applicant2SolicitorSwitchToSoleFoTest {

    @Mock
    private SwitchToSoleService switchToSoleService;

    @InjectMocks
    private Applicant2SolicitorSwitchToSoleFo applicant2SolicitorSwitchToSoleFo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorSwitchToSoleFo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_FO);
    }

    @Test
    void shouldReturnErrorOnMidEventIfNoIsSelectedOnDoesApplicant2WantToApplyForFinalOrder() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant2WantToApplyForFinalOrder(NO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolicitorSwitchToSoleFo.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("You must select 'Yes' to apply for Final Order");
    }

    @Test
    void shouldNotReturnErrorsOnMidEventIfYesIsSelectedOnDoesApplicant2WantToApplyForFinalOrder() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant2WantToApplyForFinalOrder(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolicitorSwitchToSoleFo.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldClearFieldDataInAboutToStartCallback() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant2WantToApplyForFinalOrder(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolicitorSwitchToSoleFo.aboutToStart(caseDetails);

        assertThat(response.getData().getFinalOrder().getDoesApplicant2WantToApplyForFinalOrder()).isNull();
    }

    @Test
    void shouldPopulateSwitchToSoleFinalOrderFieldsSwitchRolesAndSwitchUserData() {
        CaseData caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().doesApplicant1WantToApplyForFinalOrder(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolicitorSwitchToSoleFo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getFinalOrder().getFinalOrderSwitchedToSole()).isEqualTo(YES);

        verify(switchToSoleService).switchUserRoles(caseData, TEST_CASE_ID);
        verify(switchToSoleService).switchApplicantData(caseData);
    }
}
