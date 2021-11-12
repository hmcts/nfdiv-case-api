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
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant1ResubmitNotification.THEIR_EMAIL_ADDRESS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;
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

    private String app2CheckJointAnswers = "/applicant2/check-your-joint-application";

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);
        final Map<String, String> templateVars = new HashMap<>();
        templateVars.putAll(getConfigTemplateVars());
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_APPLICANT_2_USER_EMAIL),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_APPLICANT_2_USER_EMAIL),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DIVORCE_TEST_URL + app2CheckJointAnswers)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DISSOLUTION_TEST_URL + app2CheckJointAnswers)
            )),
            eq(ENGLISH)
        );
    }
}
