package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT1_APPLICANT2_REJECTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_APPLICANT2_REJECTED;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_A_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_YOUR_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
class Applicant2NotBrokenNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant2NotBrokenNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validJointApplicant1CaseData();

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_REJECTED),
            argThat(allOf(
                hasEntry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_DIVORCE)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDivorceContent() {
        CaseData data = validJointApplicant1CaseData();

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_REJECTED),
            argThat(allOf(
                hasEntry(FOR_A_APPLICATION, "for a divorce")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT1_APPLICANT2_REJECTED),
            argThat(allOf(
                hasEntry(DIVORCE_OR_END_CIVIL_PARTNERSHIP, TO_END_CIVIL_PARTNERSHIP)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant2WithDissolutionContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICANT2_APPLICANT2_REJECTED),
            argThat(allOf(
                hasEntry(FOR_YOUR_APPLICATION, TO_END_CIVIL_PARTNERSHIP),
                hasEntry(FOR_A_APPLICATION, TO_END_CIVIL_PARTNERSHIP)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }
}
