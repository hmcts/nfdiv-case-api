package uk.gov.hmcts.divorce.bulkaction.ccd.event;

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
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction.FailedBulkCaseRemover;
import uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Listed;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.CASEWORKER_SCHEDULE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_BULK_LIST_ERRORED_CASES;
import static uk.gov.hmcts.divorce.systemupdate.service.BulkCaseValidationService.ERROR_HEARING_DATE_IN_PAST;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseworkerScheduleCaseTest {

    private static final Long BULK_CASE_REFERENCE = 1234123412341234L;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private ScheduleCaseService scheduleCaseService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FailedBulkCaseRemover failedBulkCaseRemover;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private BulkCaseValidationService bulkCaseValidationService;

    @InjectMocks
    private CaseworkerScheduleCase scheduleCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        scheduleCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SCHEDULE_CASE);
    }

    @Test
    void shouldValidateAndErrorIfCasesHaveErrorInBulkList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkDetails();

        when(bulkCaseValidationService.validateBulkListErroredCases(details))
            .thenReturn(List.of(ERROR_BULK_LIST_ERRORED_CASES));

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response =
            scheduleCase.aboutToStart(details);

        assertThat(response.getErrors()).containsExactly(ERROR_BULK_LIST_ERRORED_CASES);

    }

    @Test
    void shouldPopulateErrorMessageWhenAboutToSubmitIsTriggeredAndValidationFails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkDetails();

        setupValidatorMock(List.of(ERROR_HEARING_DATE_IN_PAST));

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly(ERROR_HEARING_DATE_IN_PAST);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenAboutToSubmitIsTriggeredAndValidationSucceeds() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkDetails();

        setupValidatorMock(Collections.emptyList());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSuccessfullyUpdateCasesInBulkWithCourtHearingDetails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkDetails();

        setupUserAuthMocks(CITIZEN);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
        verifyNoInteractions(bulkTriggerService);
    }

    @Test
    void shouldSuccessfullyLinkCaseWithBulkListWhenRoleIsCaseWorker() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = getBulkDetails();

        setupUserAuthMocks(CASE_WORKER);
        final User systemUser = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().uid(SYSTEM_USER_USER_ID).build());
        setupSystemAuthMocks(systemUser);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
        verify(bulkTriggerService).bulkTrigger(
                details.getData().getBulkListCaseDetails(),
                SYSTEM_LINK_WITH_BULK_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_LINK_WITH_BULK_CASE),
                systemUser,
                TEST_SERVICE_AUTH_TOKEN);

        verify(failedBulkCaseRemover).removeFailedCasesFromBulkListCaseDetails(
            any(), eq(details), eq(systemUser), eq(TEST_SERVICE_AUTH_TOKEN)
        );
    }

    private void setupUserAuthMocks(UserRole role) {
        final User user = new User(AUTH_HEADER_VALUE, UserInfo.builder().roles(List.of(role.getRole())).build());
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamService.retrieveUser(TEST_AUTHORIZATION_TOKEN)).thenReturn(user);
    }

    private void setupSystemAuthMocks(User systemUser) {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    private void setupValidatorMock(List<String> errors) {
        when(bulkCaseValidationService.validateData(any(), any(), any()))
            .thenReturn(errors);
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> getBulkDetails() {
        return CaseDetails.<BulkActionCaseData, BulkActionState>builder()
            .data(BulkActionCaseData.builder().dateAndTimeOfHearing(NOW).court(BIRMINGHAM).build())
            .state(Listed)
            .id(BULK_CASE_REFERENCE)
            .build();
    }
}
