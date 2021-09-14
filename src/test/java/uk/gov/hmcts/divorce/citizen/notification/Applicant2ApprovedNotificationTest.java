package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
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
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAID_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAY_FOR_IT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.REMINDER_ACTION_REQUIRED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class Applicant2ApprovedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant2ApprovedNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required: you"),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(PAID_FOR, " and paid")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContentAndHelpWithFees() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required: you"),
                hasEntry(PAY_FOR, ""),
                hasEntry(PAID_FOR, "")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAY_FOR_IT, PAY_FOR_IT),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(PAID_FOR, PAID_FOR),
                hasEntry(PAY_FOR_IT, PAY_FOR_IT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContentAndHelpWithFees() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAY_FOR_IT, ""),
                hasEntry(PAY_FOR, ""),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(PAID_FOR, "")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant1WithDivorceContentWithApplicant2HelpWithFeesNo() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required: you"),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(PAID_FOR, " and paid")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentWithApplicant2HelpWithFeesNo() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAY_FOR_IT, PAY_FOR_IT),
                hasEntry(PAY_FOR, PAY_FOR),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER)),
                hasEntry(PAID_FOR, PAID_FOR),
                hasEntry(PAY_FOR_IT, PAY_FOR_IT)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant1WithDivorceContentWithApplicant2DeniedHwf() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1WithDeniedHwf(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required:"),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContentWithApplicant2DeniedHwf() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1WithDeniedHwf(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF),
            argThat(allOf(
                hasEntry(REMINDER_ACTION_REQUIRED, "Action required:"),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }
}

