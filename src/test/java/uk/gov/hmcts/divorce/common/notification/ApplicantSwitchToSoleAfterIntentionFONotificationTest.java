package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class ApplicantSwitchToSoleAfterIntentionFONotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ApplicantSwitchToSoleAfterIntentionFONotification notification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfJointApplicationAndApplicant1IntendedToSwitchToSoleFO() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("App1 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant1()));

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1());
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfApplicant1DoesntIntendToSwitchToSole() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(emptySet())
            .build());

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant1SolicitorNotificationIfApp1IntendsToSwitchToSoleIsNull() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .build());

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldSendApplicant2SolicitorNotificationIfJointApplicationAndApplicant2IntendedToSwitchToSoleFO() {
        CaseData data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("App1 Sol")
            .reference("12344")
            .email(TEST_SOLICITOR_EMAIL)
            .build());
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2()))
            .thenReturn(solicitorTemplateVars(data, data.getApplicant2()));

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(APPLICANT_SOLICITOR_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2());
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfApp2IntendsToSwitchToSoleIsNull() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .build());

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2SolicitorNotificationIfApplicant2DoesntIntendToSwitchToSole() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2IntendsToSwitchToSole(emptySet())
            .build());

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldSendApplicant1NotificationIfJointApplicationAndApplicant1IntendedToSwitchToSoleFO() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitorRepresented(NO);
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant1NotificationIfJointApplicationAndApplicant1IntendedToSwitchToSoleFOWelsh() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setEmail(TEST_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant1().setSolicitorRepresented(NO);
        data.getApplicant1().setLanguagePreferenceWelsh(YES);
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant1DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldNotSendApplicant1NotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant1NotificationIfApplicant1DoesntIntendToSwitchToSole() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant1IntendsToSwitchToSole(emptySet())
            .build());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant1NotificationIfApp1IntendsToSwitchToSoleIsNull() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .build());

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldSendApplicant2NotificationIfJointApplicationAndApplicant2IntendedToSwitchToSoleFO() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setSolicitorRepresented(NO);
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant2NotificationIfJointApplicationAndApplicant2IntendedToSwitchToSoleFOWelsh() {
        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplication().setIssueDate(LocalDate.of(2022, 8, 10));
        data.getApplicant2().setSolicitorRepresented(NO);
        data.getApplicant2().setLanguagePreferenceWelsh(YES);
        data.setFinalOrder(FinalOrder.builder()
            .dateApplicant2DeclaredIntentionToSwitchToSoleFo(getExpectedLocalDate().minusDays(15))
            .applicant2IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE))
            .build());

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE_AFTER_INTENTION_FO),
            any(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verifyNoMoreInteractions(notificationService);

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendApplicant2NotificationIfSoleApplication() {
        CaseData data = caseData();
        data.setApplicationType(SOLE_APPLICATION);

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2NotificationIfApplicant2DoesntIntendToSwitchToSole() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .applicant2IntendsToSwitchToSole(emptySet())
            .build());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    void shouldNotSendApplicant2NotificationIfApp2IntendsToSwitchToSoleIsNull() {
        CaseData data = caseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setFinalOrder(FinalOrder.builder()
            .build());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
