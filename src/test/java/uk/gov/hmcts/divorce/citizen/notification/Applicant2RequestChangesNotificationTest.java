package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
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
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_APPLICANT2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PARTNER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(SpringExtension.class)
public class Applicant2RequestChangesNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private Applicant2RequestChangesNotification notification;

    @Test
    void shouldSendEmailWithDivorceContent() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant(Gender.FEMALE));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant1())).thenReturn("husband");

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            argThat(allOf(
                hasEntry(PARTNER, "husband"),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "for divorce")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsFor(data);
        verify(commonContent).getTheirPartner(data, data.getApplicant1());
    }

    @Test
    void shouldSendEmailWithDissolutionContent() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.setApplicant2(getApplicant(Gender.MALE));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsFor(data)).thenReturn(templateVars);
        when(commonContent.getTheirPartner(data, data.getApplicant1())).thenReturn("civil partner");

        notification.send(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(JOINT_APPLICANT2_REQUEST_CHANGES),
            argThat(allOf(
                hasEntry(PARTNER, "civil partner"),
                hasEntry(APPLICATION.toLowerCase(Locale.ROOT), "to end your civil partnership")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsFor(data);
        verify(commonContent).getTheirPartner(data, data.getApplicant1());
    }
}
