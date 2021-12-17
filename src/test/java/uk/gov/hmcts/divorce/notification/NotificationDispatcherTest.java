package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @InjectMocks
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldNotifyApplicant1SolicitorIfRepresented() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        final ApplicantNotification applicantNotification = mock(ApplicantNotification.class);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant1OfflineIfIsApplicant1OffLineTrue() {

        final long caseId = 1L;
        final CaseData caseData = mock(CaseData.class);
        final Applicant applicant1 = mock(Applicant.class);
        final Applicant applicant2 = mock(Applicant.class);
        final Application application = mock(Application.class);
        final ApplicantNotification applicantNotification = mock(ApplicantNotification.class);

        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(applicant1.isRepresented()).thenReturn(false);
        when(caseData.getApplicant2()).thenReturn(applicant2);
        when(applicant2.isRepresented()).thenReturn(true);
        when(caseData.getApplication()).thenReturn(application);
        when(application.isApplicant1OffLine()).thenReturn(true);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant1Offline(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant1IfNotRepresented() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .build();

        final ApplicantNotification applicantNotification = mock(ApplicantNotification.class);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant1(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2SolicitorIfRepresented() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        final ApplicantNotification applicantNotification = mock(ApplicantNotification.class);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2OfflineIsApplicant1OffLineTrue() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .application(Application.builder()
                .applicant1KnowsApplicant2EmailAddress(NO)
                .build())
            .build();

        final ApplicantNotification applicantNotification = mock(ApplicantNotification.class);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2Offline(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2IfNotRepresented() {

        final long caseId = 1L;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .build();

        final ApplicantNotification applicantNotification = mock(ApplicantNotification.class);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2(caseData, caseId);
    }
}