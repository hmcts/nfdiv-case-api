package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICANT_2_COMMENTS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_THE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.THE_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(SpringExtension.class)
class Applicant2RequestChangesNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private Applicant2RequestChangesNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DIVORCE_URL, "sign in divorce link"));

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "for divorce"),
                hasEntry(APPLICANT_2_COMMENTS, "Not correct!"),
                hasEntry(FOR_THE_APPLICATION, THE_DIVORCE)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DISSOLUTION_URL, "sign in dissolution link"));

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership"),
                hasEntry(APPLICANT_2_COMMENTS, "Not correct!"),
                hasEntry(FOR_THE_APPLICATION, TO_END_CIVIL_PARTNERSHIP)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getCaseInvite().setApplicant2InviteEmailAddress(TEST_USER_EMAIL);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "for divorce")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getCaseInvite().setApplicant2InviteEmailAddress(TEST_USER_EMAIL);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            argThat(allOf(
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }
}
