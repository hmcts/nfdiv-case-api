package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class Applicant1CanSwitchToSoleNotificationTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant1CanSwitchToSoleNotification applicant1CanSwitchToSoleNotification;

    @Test
    public void shouldSendEmailToApplicant1WhenJointApplication() {

        CaseData data = validJointApplicant1CaseData();

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        applicant1CanSwitchToSoleNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT_CAN_SWITCH_TO_SOLE),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    public void shouldSendEmailInWelshToApplicant1WhenJointApplication() {

        CaseData data = validJointApplicant1CaseData();
        data.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(divorceTemplateVars);

        applicant1CanSwitchToSoleNotification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT_CAN_SWITCH_TO_SOLE),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    public void shouldNotSendEmailToApplicant1WhenSoleApplication() {

        CaseData data = validApplicant1CaseData();

        applicant1CanSwitchToSoleNotification.sendToApplicant1(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    public void shouldSendEmailToApplicant1SolicitorWhenJointApplication() {

        CaseData data = validJointApplicant1CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        data.getApplicant1().setSolicitorRepresented(YES);
        data.getApplicant1().setSolicitor(Solicitor
            .builder()
                .email(TEST_SOLICITOR_EMAIL)
            .build());

        Map<String, String> templateVars = new HashMap<>(solicitorTemplateVars(data, data.getApplicant1()));
        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1()))
            .thenReturn(templateVars);

        applicant1CanSwitchToSoleNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1());
    }

    @Test
    public void shouldNotSendEmailToApplicant1SolicitorWhenSoleApplication() {

        CaseData data = validApplicant1CaseData();
        data.getApplicant1().setSolicitorRepresented(YES);

        applicant1CanSwitchToSoleNotification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
