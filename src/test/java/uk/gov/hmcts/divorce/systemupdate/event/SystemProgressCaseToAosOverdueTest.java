package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.notification.AosReminderNotifications;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SystemProgressCaseToAosOverdueTest {

    @Mock
    private AosReminderNotifications aosReminderNotifications;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemProgressCaseToAosOverdue systemProgressCaseToAosOverdue;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemProgressCaseToAosOverdue.configure(configBuilder);
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SystemProgressCaseToAosOverdue.SYSTEM_PROGRESS_TO_AOS_OVERDUE);
    }

    @Test
    void shouldSendBothEmailsForCitizenApplicationIfAccessCodeAndEmailAddressAreNotNull() {
        final CaseData caseData = caseData();
        caseData.setCaseInvite(new CaseInvite("app2@email.com", "ACCESS12", null));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(aosReminderNotifications, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(aosReminderNotifications);
    }

    @Test
    void shouldNotSendEmailToRespondentIfAccessCodeIsNull() {
        final CaseData caseData = caseData();
        caseData.setCaseInvite(new CaseInvite("app2@email.com", null, null));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(aosReminderNotifications, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(aosReminderNotifications);
    }

    @Test
    void shouldNotSendEmailToRespondentIfApplicant2EmailAddressIsNull() {
        final CaseData caseData = caseData();
        caseData.setCaseInvite(new CaseInvite(null, "ACCESS12", null));
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(aosReminderNotifications, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(aosReminderNotifications);
    }

    @Test
    void shouldNotSendEmailToApplicantForSolicitorApplication() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(Solicitor.builder().email("test@test.com").build());
        caseData.getApplicant1().setSolicitorRepresented(YES);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verifyNoInteractions(aosReminderNotifications);
    }
}
