package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties.OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;


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
    void shouldNotifyApplicant2OfflineIsApplicant2OffLineTrue() {

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
    void shouldNotifyApplicant2IfNotRepresentedWithCaseInviteUsingCaseDetails() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .caseInvite(CaseInvite.builder()
                .applicant2InviteEmailAddress("app2@email.com")
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant2(caseDetails);
    }

    @Test
    void shouldNotifyApplicant1SolicitorIfRepresentedUsingCaseDetails() {

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant1Solicitor(caseDetails);
    }

    @Test
    void shouldNotifyApplicant1OfflineIfIsApplicant1OffLineTrueUsingCaseDetails() {

        final CaseData caseData = mock(CaseData.class);
        final Applicant applicant1 = mock(Applicant.class);
        final Applicant applicant2 = mock(Applicant.class);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(caseData.getApplicant1()).thenReturn(applicant1);
        when(applicant1.isRepresented()).thenReturn(false);
        when(caseData.getApplicant2()).thenReturn(applicant2);
        when(applicant2.isRepresented()).thenReturn(true);
        when(applicant1.isApplicantOffline()).thenReturn(true);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant1Offline(caseDetails);
    }

    @Test
    void shouldNotifyApplicant1IfNotRepresentedUsingCaseDetails() {

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(NO)
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant1(caseDetails);
    }

    @Test
    void shouldNotifyApplicant2SolicitorIfRepresentedUsingCaseDetails() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant2Solicitor(caseDetails);
    }

    @Test
    void shouldNotifyApplicant2OfflineIsApplicant1OffLineTrueUsingCaseDetails() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .offline(YES)
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant2Offline(caseDetails);
    }

    @Test
    void shouldNotifyApplicant2IfNotRepresentedUsingCaseDetails() {

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .email("app2@email.com")
                .solicitorRepresented(NO)
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.send(applicantNotification, caseDetails);

        verify(applicantNotification).sendToApplicant2(caseDetails);
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
        verify(applicantNotification).sendToApplicant1Offline(caseData, caseId);
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
        verify(applicantNotification).sendToApplicant2Offline(caseData, caseId);
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

    @Test
    void shouldSendRequestForInformationToApplicantIfNotRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicantSolicitorIfRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOfflineApplicantIfNotRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Offline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOfflineApplicantSolicitorIfRepresentedOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(APPLICANT);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1SolicitorOffline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicant1IfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicant1SolicitorIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOfflineApplicant1IfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Offline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOfflineApplicant1SolicitorIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1SolicitorOffline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicant2IfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicant2SolicitorIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOfflineApplicant2IfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2Offline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOfflineApplicant2SolicitorIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2SolicitorOffline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToBothApplicantsIfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
        verify(applicantNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToBothOfflineApplicantsIfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(getApplicant());
        caseData.getApplicant2().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Offline(caseData, TEST_CASE_ID);
        verify(applicantNotification).sendToApplicant2Offline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToBothApplicantsSolicitorsIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
        verify(applicantNotification).sendToApplicant2Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToBothOfflineApplicantsSolicitorsIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant1().setOffline(YES);
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1SolicitorOffline(caseData, TEST_CASE_ID);
        verify(applicantNotification).sendToApplicant2SolicitorOffline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicant1SolicitorIfRepresentedAndApplicant2IfNotRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.setApplicant2(getApplicant());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
        verify(applicantNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToApplicant1IfNotRepresentedAndApplicant2SolicitorIfRepresentedOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(BOTH);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
        verify(applicantNotification).sendToApplicant2Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOtherRecipientOnSoleCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationSoleParties(OTHER);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToOtherRecipient(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationToOtherRecipientOnJointCase() {
        CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getRequestForInformationList().getRequestForInformation()
            .setRequestForInformationJointParties(RequestForInformationJointParties.OTHER);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToOtherRecipient(caseData, TEST_CASE_ID);
    }

    @Test
    void sendRequestForInformationNotificationShouldThrowExceptionWhenPartiesNotSet() {
        CaseData caseData = caseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationNotification(applicantNotification, caseData, TEST_CASE_ID);
        });
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant1() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseLetterToOfflineApplicant1() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Offline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant1Solicitor() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseLetterToOfflineApplicant1Solicitor() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1SolicitorOffline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant2() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseLetterToOfflineApplicant2() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setOffline(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2Offline(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseEmailToApplicant2Solicitor() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationResponseLetterToOfflineApplicant2Solicitor() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2SolicitorOffline(caseData, TEST_CASE_ID);
    }

    @Test
    void sendRequestForInformationResponseNotificationShouldThrowExceptionWhenPartiesNotSet() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().addRequestToList(new RequestForInformation());
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(new RequestForInformationResponse());
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponseNotification(applicantNotification, caseData, TEST_CASE_ID);
        });
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant1WhenCitizenResponds() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant1WhenSolicitorResponds() {
        CaseData caseData = caseData();
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2SOLICITOR);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant1SolicitorWhenRepresentedAndCitizenResponds() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant1SolicitorWhenRepresentedAndSolicitorResponds() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.setApplicant2(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT2);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT2SOLICITOR);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant1Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant2WhenCitizenResponds() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant2WhenSolicitorResponds() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1SOLICITOR);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant2SolicitorWhenRepresentedAndCitizenResponds() {
        CaseData caseData = caseData();
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendRequestForInformationPartnerResponseEmailToApplicant2SolicitorWhenRepresentedAndSolicitorResponds() {
        CaseData caseData = caseData();
        caseData.setApplicant1(applicantRepresentedBySolicitor());
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getRequestForInformationList().getRequestForInformation().setRequestForInformationJointParties(APPLICANT1);
        caseData.getRequestForInformationList().addRequestToList(caseData.getRequestForInformationList().getRequestForInformation());
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(caseData, RequestForInformationResponseParties.APPLICANT1SOLICITOR);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);

        verify(applicantNotification).sendToApplicant2Solicitor(caseData, TEST_CASE_ID);
    }

    @Test
    void sendRequestForInformationResponsePartnerNotificationShouldThrowExceptionWhenPartiesNotSet() {
        CaseData caseData = caseData();
        caseData.getRequestForInformationList().addRequestToList(new RequestForInformation());
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(new RequestForInformationResponse());
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        Assertions.assertThrows(NotificationTemplateException.class, () -> {
            notificationDispatcher.sendRequestForInformationResponsePartnerNotification(applicantNotification, caseData, TEST_CASE_ID);
        });
    }
}
