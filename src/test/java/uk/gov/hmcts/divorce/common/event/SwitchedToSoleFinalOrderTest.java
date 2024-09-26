package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.common.notification.SwitchedToSoleFoNotification;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrder.SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class SwitchedToSoleFinalOrderTest {

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private SwitchToSoleService switchToSoleService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SwitchedToSoleFoNotification switchedToSoleFoNotification;

    @Mock
    private GeneralReferralService generalReferralService;

    @InjectMocks
    private SwitchedToSoleFinalOrder switchedToSoleFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        switchedToSoleFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SWITCH_TO_SOLE_FO);
    }

    @Test
    void shouldNotSwitchDataAndSetApplicationTypeToSoleIfTriggeredByApplicant1() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant2("app1-token", caseId)).thenReturn(false);

        final AboutToStartOrSubmitResponse<CaseData, State> response = switchedToSoleFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getFinalOrder().getFinalOrderSwitchedToSole()).isEqualTo(YES);
    }

    @Test
    void shouldSwitchDataAndSetApplicationTypeToSoleIfTriggeredByApplicant2() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app2-token");
        when(ccdAccessService.isApplicant2("app2-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = switchedToSoleFinalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getFinalOrder().getFinalOrderSwitchedToSole()).isEqualTo(YES);

        verify(switchToSoleService).switchUserRoles(caseData, caseId);
        verify(switchToSoleService).switchApplicantData(caseData);
    }

    @Test
    void shouldSendNotificationsInSubmittedCallback() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleFinalOrder.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(switchedToSoleFoNotification, caseDetails.getData(), caseId);
    }

    @Test
    void shouldPassCaseDetailsToGeneralReferralService() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        switchedToSoleFinalOrder.submitted(caseDetails, null);

        verify(generalReferralService).caseWorkerGeneralReferral(same(caseDetails));
    }
}
