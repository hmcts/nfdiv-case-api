package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTERIM_APPLICATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
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
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(true);

        saveAndSignOutNotificationHandler.notifyApplicant(Submitted, caseData, CASE_ID, USER_TOKEN);

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
    void shouldCallSendEmailToApp1WhenNotifyApplicantIsInvokedForGivenCaseDataWhenInformationRequested() {
        CaseData caseData = validApplicant2CaseData();
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(true);

        saveAndSignOutNotificationHandler.notifyApplicant(InformationRequested, caseData, CASE_ID, USER_TOKEN);

        verify(commonContent).mainTemplateVars(caseData, CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailToApp2WhenNotifyApplicantIsInvokedForGivenCaseData() {
        CaseData caseData = validApplicant2CaseData();
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(false);


        saveAndSignOutNotificationHandler.notifyApplicant(Submitted, caseData, CASE_ID, USER_TOKEN);

        verify(commonContent).mainTemplateVars(caseData, CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailToApp2WhenNotifyApplicantIsInvokedForGivenCaseDataWhenInformationRequested() {
        CaseData caseData = validApplicant2CaseData();
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(false);


        saveAndSignOutNotificationHandler.notifyApplicant(InformationRequested, caseData, CASE_ID, USER_TOKEN);

        verify(commonContent).mainTemplateVars(caseData, CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(REQUEST_FOR_INFORMATION_SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }


    @Test
    void shouldCallSendEmailToApp1WhenNotifyApplicantIsInvokedForGivenCaseDataWhenInterimApplicationTypeIsDeemedService() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions
            .builder().interimApplicationType(InterimApplicationType.DEEMED_SERVICE).build());
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(true);


        saveAndSignOutNotificationHandler.notifyApplicant(AosOverdue, caseData, CASE_ID, USER_TOKEN);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(INTERIM_APPLICATION_SAVE_SIGN_OUT),
            argThat(allOf(
                hasEntry("interimApplicationType", "deemed service")
            )),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }

    @Test
    void shouldCallSendEmailToApp1WhenNotifyApplicantIsInvokedForGivenCaseDataWhenInterimApplicationTypeIsDeemedServiceWelsh() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.YES);
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions
            .builder().interimApplicationType(InterimApplicationType.DEEMED_SERVICE).build());
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(true);


        saveAndSignOutNotificationHandler.notifyApplicant(AosOverdue, caseData, CASE_ID, USER_TOKEN);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(INTERIM_APPLICATION_SAVE_SIGN_OUT),
            argThat(allOf(
                hasEntry("interimApplicationType", "cyflwyno tybiedig")
            )),
            eq(WELSH),
            eq(CASE_ID)
        );
    }

    @Test
    void shouldNotSendInterimApplicationNotificationForProcessServer() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YesOrNo.NO);
        caseData.getApplicant1().setInterimApplicationOptions(InterimApplicationOptions
            .builder().interimApplicationType(InterimApplicationType.PROCESS_SERVER_SERVICE).build());
        User user = new User(USER_TOKEN, UserInfo.builder().sub(TEST_USER_EMAIL).build());
        when(idamService.retrieveUser(USER_TOKEN)).thenReturn(user);
        when(ccdAccessService.isApplicant1(USER_TOKEN, CASE_ID)).thenReturn(true);


        saveAndSignOutNotificationHandler.notifyApplicant(AosOverdue, caseData, CASE_ID, USER_TOKEN);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            any(),
            eq(ENGLISH),
            eq(CASE_ID)
        );
    }
}
