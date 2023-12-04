package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.document.print.documentpack.AwaitingConditionalOrderReminderNotificationDocumentPack;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class AwaitingConditionalOrderReminderNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AwaitingConditionalOrderReminderNotificationDocumentPack awaitingConditionalOrderReminderNotificationDocumentPack;

    @Mock
    private LetterPrinter letterPrinter;

    @InjectMocks
    private AwaitingConditionalOrderReminderNotification awaitingConditionalOrderReminderNotification;

    @Test
    void shouldSendNotificationToApplicant1() {
        final CaseData caseData = caseData();

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        awaitingConditionalOrderReminderNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendNotificationInWelshToApplicant1WhenApp1LangPrefIsWelsh() {
        final CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1(),
                caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        awaitingConditionalOrderReminderNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendNotificationToApplicant2IfJointApplication() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant2(),
                caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        awaitingConditionalOrderReminderNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendNotificationInWelshToApplicant2IfJointApplicationWhenApp2LangPrefIsWelsh() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);

        when(commonContent
            .conditionalOrderTemplateVars(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant2(),
                caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        awaitingConditionalOrderReminderNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, CommonContent.YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldNotSendNotificationToApplicant2IfSoleApplication() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        awaitingConditionalOrderReminderNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendToApplicant1WhenOffline() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder().firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).offline(YES).build());
        awaitingConditionalOrderReminderNotification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(awaitingConditionalOrderReminderNotificationDocumentPack).getDocumentPack(caseData, caseData.getApplicant1());
        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            awaitingConditionalOrderReminderNotificationDocumentPack.getDocumentPack(caseData, caseData.getApplicant1()),
            awaitingConditionalOrderReminderNotificationDocumentPack.getLetterId()
        );
    }

    @Test
    void shouldSendToApplicant2WhenOfflineInJointCase() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).offline(YES).build());
        awaitingConditionalOrderReminderNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(awaitingConditionalOrderReminderNotificationDocumentPack).getDocumentPack(caseData, caseData.getApplicant2());
        verify(letterPrinter).sendLetters(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant2(),
            awaitingConditionalOrderReminderNotificationDocumentPack.getDocumentPack(caseData, caseData.getApplicant2()),
            awaitingConditionalOrderReminderNotificationDocumentPack.getLetterId()
        );
    }

    @Test
    void shouldNotSendToApplicant2WhenOfflineInSoleCase() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant2(Applicant.builder().firstName(TEST_FIRST_NAME).lastName(TEST_LAST_NAME).offline(YES).build());
        awaitingConditionalOrderReminderNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(letterPrinter);
    }

    @Test
    void shouldNotSendToApplicant1IfSentInPastSuccessfully() {
        final CaseData caseData = caseData();
        caseData.getSentNotifications()
            .setAwaitingConditionalOrderReminderNotificationSendToApplicant1(YES);

        awaitingConditionalOrderReminderNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendToApplicant2IfSentInPastSuccessfully() {
        final CaseData caseData = caseData();
        caseData.getSentNotifications()
            .setAwaitingConditionalOrderReminderNotificationSendToApplicant2(YES);

        awaitingConditionalOrderReminderNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendToApplicant1OfflineIfSentInPastSuccessfully() {
        final CaseData caseData = caseData();
        caseData.getSentNotifications()
            .setAwaitingConditionalOrderReminderNotificationSendToApplicant1Offline(YES);

        awaitingConditionalOrderReminderNotification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(letterPrinter);
    }

    @Test
    void shouldNotSendToApplicant2OfflineIfSentInPastSuccessfully() {
        final CaseData caseData = caseData();
        caseData.getSentNotifications()
            .setAwaitingConditionalOrderReminderNotificationSendToApplicant2Offline(YES);

        awaitingConditionalOrderReminderNotification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(letterPrinter);
    }
}
