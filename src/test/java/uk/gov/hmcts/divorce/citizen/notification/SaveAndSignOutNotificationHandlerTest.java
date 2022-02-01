package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class SaveAndSignOutNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Test
    void shouldCallSendEmailToApp1WhenNotifyApplicantIsInvokedForGivenCaseData() {

        saveAndSignOutNotificationHandler.notifyApplicant(validApplicant1CaseData(), true);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldCallSendEmailToApp2WhenNotifyApplicantIsInvokedForGivenCaseData() {

        saveAndSignOutNotificationHandler.notifyApplicant(validApplicant2CaseData(), false);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH)
        );
    }

}
