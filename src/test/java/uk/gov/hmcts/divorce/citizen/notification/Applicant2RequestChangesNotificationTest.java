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

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2RequestChangesNotification.APPLICANT_2_COMMENTS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NO;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getCommonTemplateVars;

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
        when(commonContent.templateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getCommonTemplateVars());
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DIVORCE_URL, "sign in divorce link"));

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES),
            argThat(allOf(
                hasEntry(APPLICANT_2_COMMENTS, "Not correct!"),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(IS_DIVORCE, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getApplication().setApplicant2ExplainsApplicant1IncorrectInformation("Not correct!");
        final Map<String, String> templateVars = getCommonTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.templateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(Map.of(SIGN_IN_DISSOLUTION_URL, "sign in dissolution link"));

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_NEED_TO_MAKE_CHANGES),
            argThat(allOf(
                hasEntry(APPLICANT_2_COMMENTS, "Not correct!"),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(IS_DIVORCE, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).templateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));
        data.getCaseInvite().setApplicant2InviteEmailAddress(TEST_USER_EMAIL);
        when(commonContent.templateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getCommonTemplateVars());

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            any(),
            eq(ENGLISH)
        );
        verify(commonContent).templateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getCaseInvite().setApplicant2InviteEmailAddress(TEST_USER_EMAIL);
        final Map<String, String> templateVars = getCommonTemplateVars();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.templateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            any(),
            eq(ENGLISH)
        );
        verify(commonContent).templateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }
}
