package uk.gov.hmcts.reform.divorce.caseapi.notification.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.caseapi.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.caseapi.service.NotificationService;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.enums.DivorceOrDissolution;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.EmailTemplateNames.SAVE_SIGN_OUT;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.SIGN_IN_URL;

@ExtendWith(MockitoExtension.class)
class SaveAndSignOutNotificationHandlerTest {
    public static final String SOME_URL = "someurl";
    public static final String TEST_COURT_EMAIL = "testcourt@test.com";
    public static final String DIV_COURT_EMAIL = "divCourtEmail";

    public static final String TEST_USER_EMAIL = "test@test.com";
    public static final String TEST_FIRST_NAME = "test_first_name";
    public static final String TEST_LAST_NAME = "test_last_name";

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
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), "70dd0a1e-047f-4baa-993a-e722db17d8ac")
            ));

        saveAndSignOutNotificationHandler.notifyApplicant(caseData());

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq("70dd0a1e-047f-4baa-993a-e722db17d8ac"),
            anyMap(),
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
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), "70dd0a1e-047f-4baa-993a-e722db17d8ac")
            ));

        doThrow(new RuntimeException("all template params not set"))
            .when(notificationService).sendEmail(any(), any(), anyMap(), any());

        final CaseData caseData = caseData();

        Throwable thrown = assertThrows(RuntimeException.class,
            () -> {
                saveAndSignOutNotificationHandler.notifyApplicant(caseData);
            }
        );

        assertThat(thrown.getMessage(), is("all template params not set"));

        verify(emailTemplatesConfig).getTemplateVars();
        verify(emailTemplatesConfig).getTemplates();
        verifyNoMoreInteractions(emailTemplatesConfig, notificationService);
    }


    private Map<String, Map<String, String>> getConfigTemplateVars() {
        return Map.of(
            SAVE_SIGN_OUT.name(), Map.of(SIGN_IN_URL, SOME_URL,
                DIV_COURT_EMAIL, TEST_COURT_EMAIL
            )
        );
    }

    private CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setD8PetitionerFirstName(TEST_FIRST_NAME);
        caseData.setD8PetitionerLastName(TEST_LAST_NAME);
        caseData.setD8PetitionerEmail(TEST_USER_EMAIL);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        return caseData;
    }
}
