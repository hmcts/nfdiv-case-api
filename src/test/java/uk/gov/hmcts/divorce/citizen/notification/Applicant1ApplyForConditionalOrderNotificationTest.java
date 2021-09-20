package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1ApplyForConditionalOrderNotification;
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
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLY_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(SpringExtension.class)
public class Applicant1ApplyForConditionalOrderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant1ApplyForConditionalOrderNotification notification;

    @Test
    void shouldSendEmailToApplicant1WithDivorceContent() {
        CaseData data = validJointApplicant1CaseData();

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, String.valueOf(1234567890123456L))
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

        notification.sendToApplicant1(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLY_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, String.valueOf(1234567890123456L))
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }
}
