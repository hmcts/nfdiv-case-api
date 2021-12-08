package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SystemProgressCaseToAosOverdueTest {

    @Mock
    private ApplicationIssuedNotification applicationIssuedNotification;

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
        caseData.getCaseInvite().setApplicant2InviteEmailAddress("app2@email.com");
        caseData.getCaseInvite().setAccessCode("ACCESS12");
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(applicationIssuedNotification).sendReminderToSoleRespondent(caseData, 1L);
        verify(applicationIssuedNotification).sendPartnerNotRespondedToSoleApplicant(caseData, 1L);
        verifyNoMoreInteractions(applicationIssuedNotification);
    }

    @Test
    void shouldNotSendEmailToRespondentIfAccessCodeIsNull() {
        final CaseData caseData = caseData();
        caseData.getCaseInvite().setApplicant2InviteEmailAddress("app2@email.com");
        caseData.getCaseInvite().setAccessCode(null);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(applicationIssuedNotification).sendPartnerNotRespondedToSoleApplicant(caseData, 1L);
        verifyNoMoreInteractions(applicationIssuedNotification);
    }

    @Test
    void shouldNotSendEmailToRespondentIfApplicant2EmailAddressIsNull() {
        final CaseData caseData = caseData();
        caseData.getCaseInvite().setApplicant2InviteEmailAddress(null);
        caseData.getCaseInvite().setAccessCode("ACCESS12");
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verify(applicationIssuedNotification).sendPartnerNotRespondedToSoleApplicant(caseData, 1L);
        verifyNoMoreInteractions(applicationIssuedNotification);
    }

    @Test
    void shouldNotSendEmailToApplicantForSolicitorApplication() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitor(Solicitor.builder().email("test@test.com").build());
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        systemProgressCaseToAosOverdue.aboutToSubmit(details, details);

        verifyNoInteractions(applicationIssuedNotification);
    }
}
