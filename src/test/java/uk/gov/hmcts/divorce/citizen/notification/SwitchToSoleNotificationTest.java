package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.APPLICANT_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICATION_ENDED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class SwitchToSoleNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SwitchToSoleNotification notification;

    @Test
    void shouldSendApplicant1SwitchToSoleEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        final Map<String, String> templateVars = Map.of(IS_DIVORCE, YES, IS_DISSOLUTION, NO);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        notification.sendApplicant1SwitchToSoleNotificationToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
                )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant1SwitchToSoleEmailToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        final Map<String, String> templateVars = Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);

        notification.sendApplicant1SwitchToSoleNotificationToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());

    }

    @Test
    void shouldSendApplicant1SwitchToSoleEmailToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = Map.of(IS_DIVORCE, YES, IS_DISSOLUTION, NO);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendApplicant1SwitchToSoleNotificationToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICATION_ENDED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant1SwitchToSoleEmailToApplicant2WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendApplicant1SwitchToSoleNotificationToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(JOINT_APPLICATION_ENDED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant2SwitchToSoleEmailToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        final Map<String, String> templateVars = Map.of(IS_DIVORCE, YES, IS_DISSOLUTION, NO);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        notification.sendApplicant2SwitchToSoleNotificationToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_ENDED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendApplicant2SwitchToSoleEmailToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        final Map<String, String> templateVars = Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicant2SwitchToSoleNotificationToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICATION_ENDED),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant1(), data.getApplicant2());

    }

    @Test
    void shouldSendApplicant2SwitchToSoleEmailToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = Map.of(IS_DIVORCE, YES, IS_DISSOLUTION, NO);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendApplicant2SwitchToSoleNotificationToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendApplicant2SwitchToSoleEmailToApplicant2WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES);
        when(commonContent.mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendApplicant2SwitchToSoleNotificationToApplicant2(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(APPLICANT_SWITCH_TO_SOLE),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH)
        );
        verify(commonContent).mainTemplateVars(data, 1234567890123456L, data.getApplicant2(), data.getApplicant1());
    }
}
