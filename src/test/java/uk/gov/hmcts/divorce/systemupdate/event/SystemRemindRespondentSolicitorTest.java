package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.RespondentSolicitorReminderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindRespondentSolicitor.SYSTEM_REMIND_RESPONDENT_SOLICITOR_TO_RESPOND;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;

@ExtendWith(SpringExtension.class)
public class SystemRemindRespondentSolicitorTest {

    private static final LocalDate ISSUE_DATE = LocalDate.now().minusDays(10);

    @Mock
    private RespondentSolicitorReminderNotification respondentSolicitorReminderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemRemindRespondentSolicitor remindRespondentSolicitor;

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

        final CaseData caseData = caseDataWithOrderSummary();
        caseData.getApplication().setIssueDate(ISSUE_DATE);
        caseData.getApplication().setServiceMethod(ServiceMethod.COURT_SERVICE);
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.YES);

        Solicitor solicitor = Solicitor.builder()
            .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(Organisation.builder().organisationId("ORG").build())
                .build())
            .email("sol@gm.com")
            .build();

        caseData.getApplicant2().setSolicitor(solicitor);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);
        details.setState(State.AwaitingAos);

        final AboutToStartOrSubmitResponse<CaseData, State> response = remindRespondentSolicitor.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(respondentSolicitorReminderNotification, details.getData(), details.getId());
        assertThat(response.getData().getApplication().getRespondentSolicitorReminderSent()).isEqualTo(YesOrNo.YES);
    }
}
