package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;
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
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_APPLICANT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_RESPONDENT_AOS_SUBMITTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.CIVIL_PARTNERSHIP_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PROCESS;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YOUR_UNION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAosSubmitted;

@ExtendWith(SpringExtension.class)
public class SoleAosSubmittedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private SoleAosSubmittedNotification notification;

    @Test
    void shouldSendAosNotDisputedEmailToSoleApplicantWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LocalDate.now().plusDays(141));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationNotDisputedToApplicant(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOL_APPLICANT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(YOUR_UNION, YOUR_DIVORCE),
                hasEntry(PROCESS, DIVORCE_PROCESS)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosNotDisputedEmailToSoleApplicantWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2())).thenReturn(templateVars);

        notification.sendApplicationNotDisputedToApplicant(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SOL_APPLICANT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(1234567890123456L)),
                hasEntry(YOUR_UNION, "Ending your civil partnership"),
                hasEntry(PROCESS, CIVIL_PARTNERSHIP_PROCESS)
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendAosNotDisputedEmailToSoleRespondentWithDivorceContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDueDate(LocalDate.now().plusDays(141));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendApplicationNotDisputedToRespondent(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOL_RESPONDENT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(YOUR_UNION, YOUR_DIVORCE.toLowerCase(Locale.ROOT))
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendAosNotDisputedEmailToSoleRespondentWithDissolutionContent() {
        CaseData data = validCaseDataForAosSubmitted();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setDueDate(LocalDate.now().plusDays(141));

        final HashMap<String, String> templateVars = new HashMap<>();

        when(commonContent.templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1())).thenReturn(templateVars);

        notification.sendApplicationNotDisputedToRespondent(data, 1234567890123456L);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(SOL_RESPONDENT_AOS_SUBMITTED),
            argThat(allOf(
                hasEntry(YOUR_UNION, "ending your civil partnership")
            )),
            eq(ENGLISH)
        );

        verify(commonContent).templateVarsForApplicant(data, data.getApplicant2(), data.getApplicant1());
    }
}
