package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.exception.NotificationException;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.citizen.notification.EmailTemplateNames.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.common.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DISSOLUTION_URL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SIGN_IN_DIVORCE_URL;
import static uk.gov.hmcts.divorce.util.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SaveAndSignOutNotificationHandlerTest {
    public static final String SOME_URL = "someurl";
    public static final String SOME_OTHER_URL = "someotherurl";
    public static final String TEST_COURT_EMAIL = "testcourt@test.com";
    public static final String DIV_COURT_EMAIL = "divCourtEmail";

    public static final String TEST_USER_EMAIL = "test@test.com";

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @Test
    void shouldCallSendEmailWhenNotifyApplicantIsInvokedForGivenCaseData() {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        when(emailTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), "3aff9dc0-0188-42f6-a0be-fc5daa404d9a")
            ));

        saveAndSignOutNotificationHandler.notifyApplicant(caseData());

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq("3aff9dc0-0188-42f6-a0be-fc5daa404d9a"),
            anyMap(),
            eq(ENGLISH)
        );
        verify(emailTemplatesConfig).getTemplateVars();
        verify(emailTemplatesConfig).getTemplates();
        verifyNoMoreInteractions(emailTemplatesConfig, notificationService);
    }

    @Test
    void shouldSetTheAppropriateFieldsForDissolutionCases() {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        when(emailTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), "3aff9dc0-0188-42f6-a0be-fc5daa404d9a")
            ));

        CaseData caseData = caseData();
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        saveAndSignOutNotificationHandler.notifyApplicant(caseData);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq("3aff9dc0-0188-42f6-a0be-fc5daa404d9a"),
            argThat(allOf(hasEntry("signin url", SOME_OTHER_URL))), // NOSONAR
            eq(ENGLISH)
        );
        verify(emailTemplatesConfig).getTemplateVars();
        verify(emailTemplatesConfig).getTemplates();
        verifyNoMoreInteractions(emailTemplatesConfig, notificationService);
    }

    @Test
    void shouldThrowExceptionWhenExceptionIsThrownWhileSendingEmail() {
        when(emailTemplatesConfig.getTemplateVars()).thenReturn(getConfigTemplateVars());

        when(emailTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), "3aff9dc0-0188-42f6-a0be-fc5daa404d9a")
            ));

        doThrow(new NotificationException(
                new NotificationClientException("all template params not set")
            )
        )
            .when(notificationService).sendEmail(any(), any(), anyMap(), any());

        Throwable thrown = assertThrows(NotificationException.class,
            () -> {
                saveAndSignOutNotificationHandler.notifyApplicant(caseData());
            }
        );

        assertThat(thrown.getCause().getMessage(), is("all template params not set"));

        verify(emailTemplatesConfig).getTemplateVars();
        verify(emailTemplatesConfig).getTemplates();
        verifyNoMoreInteractions(emailTemplatesConfig, notificationService);
    }


    private Map<String, Map<String, String>> getConfigTemplateVars() {
        return Map.of(
            SAVE_SIGN_OUT.name(), Map.of(
                SIGN_IN_DIVORCE_URL, SOME_URL,
                SIGN_IN_DISSOLUTION_URL, SOME_OTHER_URL,
                DIV_COURT_EMAIL, TEST_COURT_EMAIL
            )
        );
    }
}
