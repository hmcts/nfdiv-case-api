package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_SOLICITOR_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicTemplateVars;

@ExtendWith(MockitoExtension.class)
public class RespondentSolicitorReminderNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private RespondentSolicitorReminderNotification reminderNotification;

    @Test
    public void shouldSendNotificationToRespondentSolicitorForDivorce() {
        CaseData data = caseData();
        data.getApplication().setIssueDate(LocalDate.of(2021, 4, 5));
        data.setDueDate(LocalDate.of(2021, 4, 21));
        data.getApplicant2().setSolicitor(Solicitor.builder()
                .name("sol")
                .reference("ref")
                .email("sol@gm.com")
            .build());
        when(commonContent.basicTemplateVars(data, TEST_CASE_ID))
            .thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("test-url");

        reminderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("sol@gm.com"),
            eq(RESPONDENT_SOLICITOR_HAS_NOT_RESPONDED),
            argThat(allOf(
                hasEntry(ISSUE_DATE, "5 April 2021"),
                hasEntry(SUBMISSION_RESPONSE_DATE, "21 April 2021"),
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, "yes"),
                hasEntry(IS_DISSOLUTION, "no"),
                hasEntry(SOLICITOR_NAME, "sol"),
                hasEntry(SOLICITOR_REFERENCE, "ref"),
                hasEntry(SIGN_IN_URL, "test-url")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    public void shouldSendNotificationToRespondentSolicitorForDissolution() {
        CaseData data = caseData();
        data.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        data.getApplication().setIssueDate(LocalDate.of(2021, 4, 5));
        data.setDueDate(LocalDate.of(2021, 4, 21));
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("sol")
            .email("sol@gm.com")
            .build());
        when(commonContent.basicTemplateVars(data, TEST_CASE_ID))
            .thenReturn(getBasicTemplateVars());
        when(commonContent.getProfessionalUsersSignInUrl(TEST_CASE_ID))
            .thenReturn("test-url");

        reminderNotification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("sol@gm.com"),
            eq(RESPONDENT_SOLICITOR_HAS_NOT_RESPONDED),
            argThat(allOf(
                hasEntry(ISSUE_DATE, "5 April 2021"),
                hasEntry(SUBMISSION_RESPONSE_DATE, "21 April 2021"),
                hasEntry(APPLICATION_REFERENCE, FORMATTED_TEST_CASE_ID),
                hasEntry(IS_DIVORCE, "no"),
                hasEntry(IS_DISSOLUTION, "yes"),
                hasEntry(SOLICITOR_NAME, "sol"),
                hasEntry(SOLICITOR_REFERENCE, "Not provided"),
                hasEntry(SIGN_IN_URL, "test-url")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
