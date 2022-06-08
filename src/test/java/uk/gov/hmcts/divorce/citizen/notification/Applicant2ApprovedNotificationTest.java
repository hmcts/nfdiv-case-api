package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2ApprovedNotification.PAYS_FEES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_APPROVED_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPROVED_APPLICANT1_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
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

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAYS_FEES, YES)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContentAndHelpWithFees() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAYS_FEES, NO)
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAYS_FEES, YES),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendAppropriateEmailToApplicant2WhenApplicant1IsRepresented() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED_SOLICITOR),
            argThat(allOf(
                hasEntry(PAYS_FEES, YES),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContentAndHelpWithFees() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAYS_FEES, NO),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContentWithApplicant2HelpWithFeesNo() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant2().setEmail(null);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_APPROVED),
            argThat(allOf(
                hasEntry(PAYS_FEES, YES),
                hasEntry(SUBMISSION_RESPONSE_DATE, LOCAL_DATE.format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1WithDivorceContentWithApplicant2DeniedHwf() {
        CaseData data = validApplicant2CaseData();
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContentWithApplicant2DeniedHwf() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setDueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT2_APPROVED_APPLICANT1_SOLICITOR),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailToApplicant1SolicitorWithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant1().setSolicitorRepresented(YesOrNo.YES);
        data.getApplicant1().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME).build());

        notification.sendToApplicant1Solicitor(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT2_APPROVED_APPLICANT1_SOLICITOR),
            anyMap(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldSendEmailInWelshLanguageToApplicant1WhenRespondentApprovedAndLanguagePreferenceIsWelsh() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DIVORCE);
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED),
            anyMap(),
            eq(WELSH)
        );

        verify(commonContent).getPartnerWelshContent(data, data.getApplicant2());
    }

    @Test
    void shouldSendEmailInWelshLanguageToApplicant1WhenHWFDeniedAndLanguagePreferenceIsWelsh() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DIVORCE);
        data.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        data.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);
        data.setDueDate(LOCAL_DATE);
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.getPartnerWelshContent(data, data.getApplicant2())).thenReturn("gŵr");

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_APPROVED_WITHOUT_HWF),
            argThat(allOf(
                hasEntry(PARTNER, "gŵr")
            )),
            eq(WELSH)
        );

        verify(commonContent).getPartnerWelshContent(data, data.getApplicant2());
    }
}

