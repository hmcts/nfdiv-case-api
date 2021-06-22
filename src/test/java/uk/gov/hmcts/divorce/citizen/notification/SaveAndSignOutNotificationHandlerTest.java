package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_URL_NOTIFY_KEY;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DISSOLUTION_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SIGN_IN_DIVORCE_TEST_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConfigTemplateVars;

@ExtendWith(MockitoExtension.class)
class SaveAndSignOutNotificationHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Test
    void shouldCallSendEmailWhenNotifyApplicantIsInvokedForGivenCaseData() {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        saveAndSignOutNotificationHandler.notifyApplicant(caseData());

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            argThat(allOf(hasEntry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DIVORCE_TEST_URL))), // NOSONAR
            eq(ENGLISH)
        );
        verify(emailTemplatesConfig).getTemplateVars();
        verifyNoMoreInteractions(emailTemplatesConfig, notificationService);
    }

    @Test
    void shouldSetTheAppropriateFieldsForDissolutionCases() {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        saveAndSignOutNotificationHandler.notifyApplicant(caseData);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(SAVE_SIGN_OUT),
            argThat(allOf(hasEntry(SIGN_IN_URL_NOTIFY_KEY, SIGN_IN_DISSOLUTION_TEST_URL))), // NOSONAR
            eq(ENGLISH)
        );

        verify(emailTemplatesConfig).getTemplateVars();
        verifyNoMoreInteractions(emailTemplatesConfig, notificationService);
    }

}
