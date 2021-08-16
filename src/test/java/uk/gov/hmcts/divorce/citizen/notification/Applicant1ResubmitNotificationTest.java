package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Locale;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.dateTimeFormatter;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.THEIR_EMAIL_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class Applicant1ResubmitNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private Applicant1ResubmitNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "divorce application"),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_USER_EMAIL)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "application to end your civil partnership"),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_USER_EMAIL)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DIVORCE_TEST_URL);

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "divorce application"),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DIVORCE_TEST_URL)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();
        templateVars.put(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DISSOLUTION_TEST_URL);

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "application to end your civil partnership"),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(dateTimeFormatter)),
                hasEntry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DISSOLUTION_TEST_URL)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }
}
