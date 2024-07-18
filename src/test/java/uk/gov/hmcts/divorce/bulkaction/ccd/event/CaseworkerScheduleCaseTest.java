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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerScheduleCase.CASEWORKER_SCHEDULE_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SYSTEM_AUTHORISATION_TOKEN;

@ExtendWith(MockitoExtension.class)
public class CaseworkerScheduleCaseTest {
    @Mock
    private ScheduleCaseService scheduleCaseService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

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
    void shouldSuccessfullyUpdateCasesInBulkWithCourtHearingDetails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        UserInfo userInfo = UserInfo.builder().name("Caseworker").roles(List.of(CASE_WORKER.getRole())).build();
        final User user = new User(TEST_AUTHORIZATION_TOKEN, userInfo);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_SYSTEM_AUTHORISATION_TOKEN);
        when(idamService.retrieveUser(TEST_SYSTEM_AUTHORISATION_TOKEN)).thenReturn(user);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = scheduleCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
        verify(bulkTriggerService).bulkTrigger(details.getData().getBulkListCaseDetails(), SYSTEM_LINK_WITH_BULK_CASE,
                bulkCaseCaseTaskFactory.getCaseTask(details, SYSTEM_LINK_WITH_BULK_CASE), user, TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInFutureAndAboutToSubmitIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.now().plusDays(5))
            .build()
        );
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldPopulateErrorMessageWhenHearingDateIsInPastAndAboutToSubmitIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.now().minusHours(5))
            .build()
        );
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = scheduleCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly("Please enter a hearing date and time in the future");
    }
}
