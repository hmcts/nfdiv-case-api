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
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.solicitorTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class Applicant2CanSwitchToSoleNotificationTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant2CanSwitchToSoleNotification applicant2CanSwitchToSoleNotification;

    @Test
    public void shouldSendEmailToApplicant1WhenJointApplication() {

        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        applicant2CanSwitchToSoleNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT_CAN_SWITCH_TO_SOLE),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    public void shouldSendEmailInWelshToApplicant1WhenJointApplication() {

        CaseData data = validJointApplicant1CaseData();
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        Map<String, String> divorceTemplateVars = new HashMap<>(getMainTemplateVars());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(divorceTemplateVars);

        applicant2CanSwitchToSoleNotification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT_CAN_SWITCH_TO_SOLE),
            anyMap(),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    public void shouldNotSendEmailToApplicant1WhenSoleApplication() {

        CaseData data = validApplicant1CaseData();

        applicant2CanSwitchToSoleNotification.sendToApplicant2(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }

    @Test
    public void shouldSendEmailToApplicant2SolicitorWhenJointApplication() {

        CaseData data = validApplicant2CaseData();
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        data.getApplicant2().setSolicitorRepresented(YES);
        data.getApplicant2().setSolicitor(Solicitor
            .builder()
            .email(TEST_SOLICITOR_EMAIL)
            .build());

        Map<String, String> templateVars = new HashMap<>(solicitorTemplateVars(data, data.getApplicant2()));
        when(commonContent.solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2()))
            .thenReturn(templateVars);

        applicant2CanSwitchToSoleNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_SOLICITOR_EMAIL),
            eq(JOINT_APPLICANT_SOLICITOR_CAN_SWITCH_TO_SOLE),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).solicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2());
    }

    @Test
    public void shouldNotSendEmailToApplicant2SolicitorWhenSoleApplication() {

        CaseData data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplicant2().setSolicitorRepresented(YES);

        applicant2CanSwitchToSoleNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(commonContent);
    }
}
