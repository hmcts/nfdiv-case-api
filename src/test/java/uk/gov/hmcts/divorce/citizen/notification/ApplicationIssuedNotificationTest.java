package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_ACCOUNT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.ENDING_YOUR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_APPLICATION_VALUE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_UNION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
public class ApplicationIssuedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private ApplicationIssuedNotification notification;

    @Test
    void shouldSendEmailToSoleApplicant1WithDivorceContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToSoleApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOL_APPLICANT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION),
                hasEntry(YOUR_UNION, YOUR_DIVORCE),
                hasEntry(PROCESS, DIVORCE_PROCESS),
                hasEntry(ACCOUNT, DIVORCE_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleApplicant1WithDissolutionContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToSoleApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOL_APPLICANT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP),
                hasEntry(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP),
                hasEntry(PROCESS, CIVIL_PARTNERSHIP_PROCESS),
                hasEntry(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToSoleRespondent(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOL_RESPONDENT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(REMINDER_APPLICATION, APPLICATION),
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION),
                hasEntry(YOUR_UNION, YOUR_DIVORCE),
                hasEntry(PROCESS, DIVORCE_PROCESS),
                hasEntry(ACCOUNT, DIVORCE_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToSoleRespondent(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOL_RESPONDENT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(REMINDER_APPLICATION, APPLICATION),
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP),
                hasEntry(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP),
                hasEntry(PROCESS, CIVIL_PARTNERSHIP_PROCESS),
                hasEntry(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendReminderEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendReminderToSoleRespondent(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOL_RESPONDENT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(REMINDER_APPLICATION, REMINDER_APPLICATION_VALUE),
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION),
                hasEntry(YOUR_UNION, YOUR_DIVORCE),
                hasEntry(PROCESS, DIVORCE_PROCESS),
                hasEntry(ACCOUNT, DIVORCE_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendReminderEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForIssueApplication();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendReminderToSoleRespondent(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOL_RESPONDENT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(REMINDER_APPLICATION, REMINDER_APPLICATION_VALUE),
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP),
                hasEntry(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP),
                hasEntry(PROCESS, CIVIL_PARTNERSHIP_PROCESS),
                hasEntry(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToJointApplicant1WithDivorceContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToJointApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION),
                hasEntry(YOUR_UNION, YOUR_DIVORCE),
                hasEntry(PROCESS, DIVORCE_PROCESS),
                hasEntry(ACCOUNT, DIVORCE_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToJointApplicant1WithDissolutionContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToJointApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP),
                hasEntry(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP),
                hasEntry(PROCESS, CIVIL_PARTNERSHIP_PROCESS),
                hasEntry(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToJointApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION),
                hasEntry(YOUR_UNION, YOUR_DIVORCE),
                hasEntry(PROCESS, DIVORCE_PROCESS),
                hasEntry(ACCOUNT, DIVORCE_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));
        data.getApplication().setIssueDate(LocalDate.now());

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToJointApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICATION_ACCEPTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(SUBMISSION_RESPONSE_DATE, data.getApplication().getIssueDate().plusDays(141).format(DATE_TIME_FORMATTER)),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP),
                hasEntry(YOUR_UNION, ENDING_YOUR_CIVIL_PARTNERSHIP),
                hasEntry(PROCESS, CIVIL_PARTNERSHIP_PROCESS),
                hasEntry(ACCOUNT, CIVIL_PARTNERSHIP_ACCOUNT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }
}
