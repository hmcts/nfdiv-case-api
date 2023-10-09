package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.Applicant2ApprovedNotification.PAYS_FEES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;

@ExtendWith(MockitoExtension.class)
class JointApplicationApprovedReminderTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private JointApplicationApprovedReminder jointApplicationApprovedReminder;

    @Test
    void shouldSendReminderEmailToApplicant1WithDivorceAndPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setApplicant2(getApplicant(Gender.FEMALE));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        jointApplicationApprovedReminder.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, YES),
                hasEntry(PAYS_FEES, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDivorceAndNoPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setApplicant2(getApplicant(Gender.FEMALE));
        HelpWithFees hwf = HelpWithFees.builder().needHelp(YesOrNo.YES).build();
        data.getApplication().setApplicant1HelpWithFees(hwf);
        data.getApplication().setApplicant2HelpWithFees(hwf);
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        jointApplicationApprovedReminder.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, YES),
                hasEntry(PAYS_FEES, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDissolutionAndPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        final Map<String, String> templateVars = getMainTemplateVars();
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        jointApplicationApprovedReminder.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, YES),
                hasEntry(PAYS_FEES, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailToApplicant1WithDissolutionAndNoPaymentContent() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        HelpWithFees hwf = HelpWithFees.builder().needHelp(YesOrNo.YES).build();
        data.getApplication().setApplicant1HelpWithFees(hwf);
        data.getApplication().setApplicant2HelpWithFees(hwf);
        final HashMap<String, String> templateVars = new HashMap<>();
        templateVars.putAll(Map.of(IS_DISSOLUTION, YES, IS_DIVORCE, NO));
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        jointApplicationApprovedReminder.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, YES),
                hasEntry(PAYS_FEES, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendReminderEmailInWelshToApplicant1WhenLanguagePreferenceIsWelsh() {
        CaseData data = caseData();
        data.setDueDate(LocalDate.now());
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        HelpWithFees hwf = HelpWithFees.builder().needHelp(YesOrNo.YES).build();
        data.getApplication().setApplicant1HelpWithFees(hwf);
        data.getApplication().setApplicant2HelpWithFees(hwf);

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        jointApplicationApprovedReminder.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_APPROVED_APPLICANT1_REMINDER),
            argThat(allOf(
                hasEntry(IS_REMINDER, YES),
                hasEntry(PAYS_FEES, NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }
}
