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
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.WELSH_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.PROFESSIONAL_USERS_SIGN_IN_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;
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

    private String app2CheckJointAnswers = "applicant2/check-your-joint-application";

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);
        final Map<String, String> templateVars = new HashMap<>();
        templateVars.putAll(getConfigTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_APPLICANT_2_USER_EMAIL),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_APPLICANT_2_USER_EMAIL),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailInWelshToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(THEIR_EMAIL_ADDRESS, TEST_APPLICANT_2_USER_EMAIL),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(WELSH_DATE_TIME_FORMATTER))
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendSpecificNotificationToApplicant2WhenResubmittedByApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL, SIGN_IN_DIVORCE_TEST_URL + app2CheckJointAnswers)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendSpecificNotificationInWelshToApplicant2WhenResubmittedByApplicant1WithDivorceContentAndApp2LanguagePrefIsWelsh() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(WELSH_DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL, SIGN_IN_DIVORCE_TEST_URL + app2CheckJointAnswers)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendSpecificNotificationToApplicant2WhenResubmittedByApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL, SIGN_IN_DISSOLUTION_TEST_URL + app2CheckJointAnswers)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendSpecificNotificationToApplicant2WhenResubmittedByApplicant1SolicitorWithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE_SOLICITOR),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL, SIGN_IN_DIVORCE_TEST_URL + app2CheckJointAnswers)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendSpecificNotificationToApplicant2WhenResubmittedByApplicant1SolicitorWithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.getApplicant1().setSolicitor(
            Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build()
        );
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT1_CHANGES_MADE_SOLICITOR),
            argThat(allOf(
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(SIGN_IN_URL, SIGN_IN_DISSOLUTION_TEST_URL + app2CheckJointAnswers)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setSolicitor(
            Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build()
        );
        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToApplicant2SolicitorWithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.getApplicant2().setSolicitor(
            Solicitor.builder().name(TEST_SOLICITOR_NAME).email(TEST_SOLICITOR_EMAIL).build()
        );
        when(commonContent.basicTemplateVars(data, TEST_CASE_ID)).thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID)).thenReturn(PROFESSIONAL_USERS_SIGN_IN_URL);

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT2_SOLICITOR_APPLICANT1_CHANGES_MADE),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(SOLICITOR_NAME, TEST_SOLICITOR_NAME),
                hasEntry(SIGN_IN_URL, PROFESSIONAL_USERS_SIGN_IN_URL)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
