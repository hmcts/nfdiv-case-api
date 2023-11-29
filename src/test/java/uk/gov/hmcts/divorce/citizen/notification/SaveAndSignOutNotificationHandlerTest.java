package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(MockitoExtension.class)
class SaveAndSignOutNotificationHandlerTest {

    private static final long CASE_ID = 123456789L;
    private static final String USER_TOKEN = "dummy-user-token";

    @Mock
    private NotificationService notificationService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private IdamService idamService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Test
    void shouldCallSendEmailToApp1WhenNotifyApplicantIsInvokedForGivenCaseData() {
        CaseData caseData = validApplicant2CaseData();
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(eq(USER_TOKEN))).thenReturn(user);
        when(ccdAccessService.isApplicant1(eq(USER_TOKEN), eq(CASE_ID))).thenReturn(true);

        saveAndSignOutNotificationHandler.notifyApplicant(caseData, CASE_ID, USER_TOKEN);

        verify(commonContent).mainTemplateVars(caseData, CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailToApp2WhenNotifyApplicantIsInvokedForGivenCaseData() {
        CaseData caseData = validApplicant2CaseData();
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(eq(USER_TOKEN))).thenReturn(user);
        when(ccdAccessService.isApplicant1(eq(USER_TOKEN), eq(CASE_ID))).thenReturn(false);


        saveAndSignOutNotificationHandler.notifyApplicant(caseData, CASE_ID, USER_TOKEN);

        verify(commonContent).mainTemplateVars(caseData, CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }
}
