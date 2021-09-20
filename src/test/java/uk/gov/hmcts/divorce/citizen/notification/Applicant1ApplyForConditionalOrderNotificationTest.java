package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1ApplyForConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
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
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLY_FOR_CONDITIONAL_ORDER_LINK;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FOR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
public class Applicant1ApplyForConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private Applicant1ApplyForConditionalOrderNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validJointApplicant1CaseData();

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, String.valueOf(1234567890123456L)),
                hasEntry(APPLY_FOR_CONDITIONAL_ORDER_LINK, SIGN_IN_DIVORCE_TEST_URL),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), FOR_DIVORCE),
                hasEntry(APPLICATION_TYPE.toLowerCase(Locale.ROOT), DIVORCE_APPLICATION)
                )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendEmailToApplicant1WithDissolutionContent() {
        CaseData data = validJointApplicant1CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, String.valueOf(1234567890123456L)),
                hasEntry(APPLY_FOR_CONDITIONAL_ORDER_LINK, SIGN_IN_DISSOLUTION_TEST_URL),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), TO_END_CIVIL_PARTNERSHIP),
                hasEntry(APPLICATION_TYPE.toLowerCase(Locale.ROOT), APPLICATION_TO_END_CIVIL_PARTNERSHIP)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }
}
