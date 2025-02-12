package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_EMAIL_UPDATED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;


@ExtendWith(MockitoExtension.class)
class EmailUpdatedNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private EmailUpdatedNotification notification;

    @Test
    void shouldSendEmailToApplicant1() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(Gender.MALE));

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        notification.send(data, TEST_CASE_ID, "test@test.com", true);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_EMAIL_UPDATED),
            argThat(allOf(
                hasEntry("old email", TEST_USER_EMAIL),
                hasEntry("new email", "test@test.com")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }

    @Test
    void shouldSendEmailToApplicant2() {
        CaseData data = caseData();
        data.setApplicant2(getApplicant2(Gender.MALE));
        data.getApplicant2().setEmail("oldemail@test.com");

        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.send(data, TEST_CASE_ID, "newemail@test.com", false);

        verify(notificationService).sendEmail(
            eq("oldemail@test.com"),
            eq(CITIZEN_EMAIL_UPDATED),
            argThat(allOf(
                hasEntry("old email", "oldemail@test.com"),
                hasEntry("new email", "newemail@test.com")
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
    }
}
