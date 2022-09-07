package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.Applicant1SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2SwitchToSoleCoNotification;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenSwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder.D84WhoApplying.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder.D84WhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class CitizenSwitchedToSoleCoTest {

    @Mock
    private Applicant1SwitchToSoleCoNotification applicant1Notification;

    @Mock
    private Applicant2SwitchToSoleCoNotification applicant2Notification;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SwitchToSoleService switchToSoleService;

    @InjectMocks
    private CitizenSwitchedToSoleCo citizenSwitchedToSoleCo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenSwitchedToSoleCo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SWITCH_TO_SOLE_CO);
    }

    @Test
    void shouldSetApplicationTypeToSoleAndSendNotificationToApplicant1() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app1-token");
        when(ccdAccessService.isApplicant1("app1-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant1Notification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
    }

    @Test
    void shouldSetApplicationTypeToSoleAndSendNotificationToApplicant2() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("app2-token");
        when(ccdAccessService.isApplicant1("app2-token", caseId)).thenReturn(false);
        when(ccdAccessService.isApplicant2("app2-token", caseId)).thenReturn(true);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2Notification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getConditionalOrder().getSwitchedToSole()).isEqualTo(YES);
    }

    @Test
    void shouldSwitchUserDataAndRolesIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleService).switchCitizenUserRoles(caseId);
        verify(switchToSoleService).switchApplicantData(caseData);
    }

    @Test
    void shouldNotSwitchRolesIfApplicant2TriggeredD84SwitchToSoleAndIsNewPaperCase() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setNewPaperCase(YES);
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleService).switchApplicantData(caseData);
        verifyNoMoreInteractions(switchToSoleService);
    }

    @Test
    void shouldNotSwitchUserDataOrRolesIfApplicant1TriggeredD84SwitchToSole() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_1).build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(switchToSoleService);
    }
}
