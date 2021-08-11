package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_OVERDUE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(SpringExtension.class)
public class JointApplicationOverdueNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private JointApplicationOverdueNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setApplicant2(getApplicant(Gender.FEMALE));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationNotReviewedEmail(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_OVERDUE),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "for divorce"),
                hasEntry(REVIEW_DEADLINE_DATE, LocalDate.now().toString())
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationNotReviewedEmail(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_OVERDUE),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership"),
                hasEntry(REVIEW_DEADLINE_DATE, LocalDate.now().toString())
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDivorceAndPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setApplicant2(getApplicant(Gender.FEMALE));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationApprovedReminderToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "for divorce"),
                hasEntry(REMINDER_ACTION_REQUIRED, REMINDER_ACTION_REQUIRED),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(PAID_FOR, PAID_FOR)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDivorceAndNoPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setApplicant2(getApplicant(Gender.FEMALE));
        HelpWithFees hwf = HelpWithFees.builder().needHelp(YesOrNo.YES).build();
        data.getApplication().setApplicant1HelpWithFees(hwf);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationApprovedReminderToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "for divorce"),
                hasEntry(REMINDER_ACTION_REQUIRED, REMINDER_ACTION_REQUIRED),
                hasEntry(PAY_FOR, ""),
                hasEntry(PAID_FOR, "")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDissolutionAndPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationApprovedReminderToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership"),
                hasEntry(REMINDER_ACTION_REQUIRED, REMINDER_ACTION_REQUIRED),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(PAID_FOR, PAID_FOR)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDissolutionAndNoPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        HelpWithFees hwf = HelpWithFees.builder().needHelp(YesOrNo.YES).build();
        data.getApplication().setApplicant1HelpWithFees(hwf);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationApprovedReminderToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership"),
                hasEntry(REMINDER_ACTION_REQUIRED, REMINDER_ACTION_REQUIRED),
                hasEntry(PAY_FOR, ""),
                hasEntry(PAID_FOR, "")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }
}
