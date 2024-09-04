package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;


@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Spy
    private ApplicantNotification applicantNotification = new TestNotification();

    @InjectMocks
    private NotificationDispatcher notificationDispatcher;

    @Test
    void shouldNotifyApplicant1SolicitorIfRepresented() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant1OfflineIfIsApplicant1OffLineTrue() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = mock(CaseData.class);
        final Applicant applicant1 = mock(Applicant.class);
        final Applicant applicant2 = mock(Applicant.class);

        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(applicant1.isRepresented()).thenReturn(false);
        when(caseData.getApplicant2()).thenReturn(applicant2);
        when(applicant2.isRepresented()).thenReturn(true);
        when(applicant1.isApplicantOffline()).thenReturn(true);

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant1Offline(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant1IfNotRepresented() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .build();

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant1(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2SolicitorIfRepresented() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2OfflineIsApplicant1OffLineTrue() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .offline(YES)
                .build())
            .build();

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2Offline(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2IfNotRepresented() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email("app2@email.com")
                .solicitorRepresented(NO)
                .build())
            .build();

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2(caseData, caseId);
    }

    @Test
    void shouldNotifyApplicant2IfNotRepresentedWithCaseInvite() {

        final long caseId = TEST_CASE_ID;
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress("app2@email.com")
                .build())
            .build();

        notificationDispatcher.send(applicantNotification, caseData, caseId);

        verify(applicantNotification).sendToApplicant2(caseData, caseId);
    }

    @Test
    void shouldSendNOCForNewDigitalSolicitorNewOrg_Applicant1() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = new CaseData();
        final boolean isApplicant1 = true;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, caseId);
    }

    @Test
    void shouldSendNOCForNewDigitalSolicitorNewOrg_Applicant2() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = new CaseData();
        final boolean isApplicant1 = false;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, caseId);
    }

    @Test
    void shouldSendNOCForNewDigitalSolicitorNewOrg_WhenApplicant1EmailIsPresent() {
        final long caseId = 12345L;
        final CaseData caseData = CaseData.builder().applicant1(Applicant.builder().email("test@test.com").build()).build();
        final CaseData previousCaseData = new CaseData();
        final boolean isApplicant1 = true;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, caseId);
        verify(applicantNotification).sendToApplicant1(caseData, caseId);
    }

    @Test
    void shouldSendNOCForNewDigitalSolicitorNewOrg_WhenApplicant2EmailIsPresent() {
        final long caseId = 12345L;
        final CaseData caseData = CaseData.builder().applicant2(Applicant.builder().email("test@test.com").build()).build();
        final CaseData previousCaseData = new CaseData();
        final boolean isApplicant1 = false;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, caseId);
        verify(applicantNotification).sendToApplicant2(caseData, caseId);
    }

    @Test
    void shouldSendNOCForOrgRemoved_Applicant1() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = new CaseData();
        final boolean isApplicant1 = true;
        final NoticeType noticeType = NoticeType.ORG_REMOVED;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant1OldSolicitor(previousCaseData, caseId);
    }

    @Test
    void shouldSendNOCForOrgRemoved_Applicant2() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = new CaseData();
        final boolean isApplicant1 = false;
        final NoticeType noticeType = NoticeType.ORG_REMOVED;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant2OldSolicitor(previousCaseData, caseId);
    }

    @Test
    void shouldSendNOCToOldSolIfNewOrgAssignedAndHadRepresentationBefore_Applicant1() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = CaseData.builder().applicant1(
            Applicant.builder().solicitorRepresented(YES).build()
        ).build();
        final boolean isApplicant1 = true;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant1OldSolicitor(previousCaseData, caseId);
    }

    @Test
    void shouldSendNOCToOldSolIfNewOrgAssignedAndHadRepresentationBefore_Applicant2() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = CaseData.builder().applicant2(
            Applicant.builder().solicitorRepresented(YES).build()
        ).build();
        final boolean isApplicant1 = false;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification).sendToApplicant2OldSolicitor(previousCaseData, caseId);
    }

    @Test
    void shouldNotSendNOCToOldSolIfNewOrgAssignedAndDidNotHaveRepresentationBefore_Applicant1() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = CaseData.builder().applicant1(
            Applicant.builder().solicitorRepresented(NO).build()
        ).build();
        final boolean isApplicant1 = true;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification, never()).sendToApplicant1OldSolicitor(previousCaseData, caseId);
    }

    @Test
    void shouldNotSendNOCToOldSolIfNewOrgAssignedAndDidNotHaveRepresentationBefore_Applicant2() {
        final long caseId = 12345L;
        final CaseData caseData = new CaseData();
        final CaseData previousCaseData = CaseData.builder().applicant2(
            Applicant.builder().solicitorRepresented(NO).build()
        ).build();
        final boolean isApplicant1 = false;
        final NoticeType noticeType = NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;

        notificationDispatcher.sendNOC(applicantNotification, caseData, previousCaseData, caseId, isApplicant1, noticeType);

        verify(applicantNotification, never()).sendToApplicant2OldSolicitor(previousCaseData, caseId);
    }

    public static class TestNotification implements ApplicantNotification {
    }
}
