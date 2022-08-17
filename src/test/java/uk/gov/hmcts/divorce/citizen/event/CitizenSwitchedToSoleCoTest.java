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
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
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
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
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
    private CaseAssignmentApi caseAssignmentApi;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

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

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2Notification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getConditionalOrder().getSwitchedToSole()).isEqualTo(YES);
    }

    @Test
    void shouldSwitchUserDataIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        final UserDetails userDetails = UserDetails.builder().id(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);

        final Applicant applicant1BeforeSwitch = caseData.getApplicant1();
        final Applicant applicant2BeforeSwitch = caseData.getApplicant2();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant1()).isEqualTo(applicant2BeforeSwitch);
        assertThat(response.getData().getApplicant2()).isEqualTo(applicant1BeforeSwitch);
    }

    @Test
    void shouldSwitchUserRolesIfApplicant2TriggeredD84SwitchToSole() {
        final long caseId = 1L;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setConditionalOrder(ConditionalOrder.builder().d84WhoApplying(APPLICANT_2).build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        final CaseAssignmentUserRolesResource caseRolesResponse = CaseAssignmentUserRolesResource.builder()
            .caseAssignmentUserRoles(List.of(
                CaseAssignmentUserRole.builder().userId("1").caseRole("[APPLICANTTWO]").build(),
                CaseAssignmentUserRole.builder().userId("2").caseRole("[CREATOR]").build()
            ))
            .build();

        final UserDetails userDetails = UserDetails.builder().id(CASEWORKER_USER_ID).build();
        final User user = new User(CASEWORKER_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseAssignmentApi.getUserRoles(CASEWORKER_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, List.of(String.valueOf(caseId))))
            .thenReturn(caseRolesResponse);
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", null, CREATOR))
            .thenReturn(getCaseAssignmentRequest("2", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", null, UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("1", UserRole.APPLICANT_2));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "1", null, CREATOR))
            .thenReturn(getCaseAssignmentRequest("1", CREATOR));
        when(ccdAccessService.getCaseAssignmentRequest(caseId, "2", null, UserRole.APPLICANT_2))
            .thenReturn(getCaseAssignmentRequest("2", UserRole.APPLICANT_2));

        citizenSwitchedToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        verify(caseAssignmentApi)
            .getUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                List.of(String.valueOf(caseId))
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", CREATOR)
            );
        verify(caseAssignmentApi)
            .removeCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", UserRole.APPLICANT_2)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("1", CREATOR)
            );
        verify(caseAssignmentApi)
            .addCaseUserRoles(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                getCaseAssignmentRequest("2", UserRole.APPLICANT_2)
            );
        verifyNoMoreInteractions(caseAssignmentApi);
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

        verifyNoInteractions(idamService);
        verifyNoInteractions(authTokenGenerator);
        verifyNoInteractions(caseAssignmentApi);
    }

    private CaseAssignmentUserRolesRequest getCaseAssignmentRequest(String userId, UserRole role) {
        return CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(
                List.of(getCaseAssignmentUserRole(role.getRole(), userId))
            ).build();
    }

    private CaseAssignmentUserRoleWithOrganisation getCaseAssignmentUserRole(String role, String userId) {
        return CaseAssignmentUserRoleWithOrganisation.builder()
            .organisationId(null)
            .caseDataId(String.valueOf(1L))
            .caseRole(role)
            .userId(userId)
            .build();
    }
}
