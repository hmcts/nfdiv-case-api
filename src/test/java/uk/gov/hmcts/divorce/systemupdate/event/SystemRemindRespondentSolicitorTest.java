package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.RespondentSolicitorReminderNotification;
import uk.gov.hmcts.divorce.common.exception.InvalidOperationException;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindRespondentSolicitor.SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;

@ExtendWith(SpringExtension.class)
public class SystemRemindRespondentSolicitorTest {

    private static final LocalDate ISSUE_DATE = LocalDate.now().minusDays(10);

    @Mock
    private RespondentSolicitorReminderNotification respondentSolicitorReminderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private Clock clock;

    @InjectMocks
    private SystemRemindRespondentSolicitor remindRespondentSolicitor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(remindRespondentSolicitor, "responseReminderOffsetDays", 10);
        setMockClock(clock);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        remindRespondentSolicitor.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND);
    }

    @Test
    void shouldSendNotificationToRespondentSolicitorWhenCaseDataIsValid() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE, YesOrNo.YES, ServiceMethod.COURT_SERVICE);

        final AboutToStartOrSubmitResponse<CaseData, State> response = remindRespondentSolicitor.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(respondentSolicitorReminderNotification, details.getData(), details.getId());
        assertThat(response.getData().getApplication().getRespondentSolicitorReminderSent()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldNotSendNotificationToRespondentSolicitorWhenNotASoleApplication() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE, YesOrNo.YES, ServiceMethod.COURT_SERVICE);
        details.getData().setApplicationType(ApplicationType.JOINT_APPLICATION);

        verifyInvalidCaseData(details);
    }

    @Test
    void shouldNotSendNotificationToRespondentSolicitorWhenRespondentSolicitorIsOffline() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE, YesOrNo.YES, ServiceMethod.COURT_SERVICE);
        details.getData().getApplicant2().getSolicitor().setOrganisationPolicy(null);

        verifyInvalidCaseData(details);
    }

    @Test
    void shouldNotSendNotificationToRespondentSolicitorWhenRespondentIsNotRepresented() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE, YesOrNo.NO, ServiceMethod.COURT_SERVICE);

        verifyInvalidCaseData(details);
    }

    @Test
    void shouldNotSendNotificationToRespondentSolicitorWhenNotACourtService() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE, YesOrNo.YES, ServiceMethod.SOLICITOR_SERVICE);

        verifyInvalidCaseData(details);
    }

    @Test
    void shouldNotSendNotificationToRespondentSolicitorWhen10DaysAfterIssueHasNotPassedYet() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE.plusDays(1), YesOrNo.YES, ServiceMethod.COURT_SERVICE);

        verifyInvalidCaseData(details);
    }

    @Test
    void shouldNotSendNotificationToRespondentSolicitorWhenRespondentSolicitorEmailIdIsNotGiven() {

        CaseDetails<CaseData, State> details = buildCaseDetails(ISSUE_DATE, YesOrNo.YES, ServiceMethod.COURT_SERVICE);
        details.getData().getApplicant2().getSolicitor().setEmail(null);

        verifyInvalidCaseData(details);
    }

    private CaseDetails<CaseData, State> buildCaseDetails(LocalDate issueDate, YesOrNo isRepresented, ServiceMethod serviceMethod) {
        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.getApplication().setServiceMethod(serviceMethod);
        caseData.getApplicant2().setSolicitorRepresented(isRepresented);

        Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().organisationId("ORG").build())
                .build())
            .email("sol@gm.com")
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);
        details.setState(State.AwaitingAos);
        return details;
    }

    private void verifyInvalidCaseData(CaseDetails<CaseData, State> details) {
        Exception exception = assertThrows(
            InvalidOperationException.class, () -> remindRespondentSolicitor.aboutToSubmit(details, details));

        assertThat(exception.getMessage())
            .isEqualTo("Invalid case data for Remind Respondent Solicitor event submission for case 1");
        verifyNoInteractions(notificationDispatcher);
    }
}
