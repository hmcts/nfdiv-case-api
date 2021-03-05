package uk.gov.hmcts.reform.divorce.caseapi.service;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.caseapi.config.EmailTemplatesConfig;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.EmailTemplateNames.SAVE_SIGN_OUT;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.WELSH;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NotificationServiceTest {

    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailTemplatesConfig emailTemplatesConfig;

    @Test
    public void shouldInvokeNotificationClientToSendEmailInEnglish() throws NotificationClientException {
        notificationService.sendEmail(
            EMAIL_ADDRESS,
            SAVE_SIGN_OUT,
            null,
            ENGLISH
        );

        verify(notificationClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(ENGLISH).get(SAVE_SIGN_OUT.name())),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString());
    }

    @Test
    public void shouldInvokeNotificationClientToSendEmailInWelsh() throws NotificationClientException {
        notificationService.sendEmail(
            EMAIL_ADDRESS,
            SAVE_SIGN_OUT,
            null,
            WELSH
        );

        verify(notificationClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(WELSH).get(SAVE_SIGN_OUT.name())),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString());
    }

    @Test
    public void shouldThrowNotificationExceptionWhenClientFailsToSendEmail()
        throws NotificationClientException {
        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        assertThatThrownBy(() -> notificationService.sendEmail(
            EMAIL_ADDRESS,
            SAVE_SIGN_OUT,
            null,
            ENGLISH
            )
        )
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");


        verify(notificationClient).sendEmail(
            eq(emailTemplatesConfig.getTemplates().get(WELSH).get(SAVE_SIGN_OUT.name())),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString());
    }
}
