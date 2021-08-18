package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

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
        final var userDetails = UserDetails.builder()
            .email("app1@test.com")
            .id("app1")
            .build();

        saveAndSignOutNotificationHandler.notifyApplicant(caseData(), userDetails);

        verify(notificationService).sendEmail(
            eq("app1@test.com"),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH)
        );
    }

    @Test
    void shouldCallSendEmailToApp2WhenNotifyApplicantIsInvokedForGivenCaseData() {
        final var userDetails = UserDetails.builder()
            .email("app2@test.com")
            .id("app2")
            .build();

        final var caseData = caseData();
        caseData.setCaseInvite(CaseInvite.builder().applicant2UserId("app").build());
        saveAndSignOutNotificationHandler.notifyApplicant(caseData, userDetails);

        verify(notificationService).sendEmail(
            eq("app2@test.com"),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH)
        );
    }

}
