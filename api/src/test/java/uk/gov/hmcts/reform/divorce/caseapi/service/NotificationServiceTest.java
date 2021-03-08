package uk.gov.hmcts.reform.divorce.caseapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.WELSH;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NotificationServiceTest {

    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private NotificationService notificationService;

    @Test
    public void shouldInvokeNotificationClientToSendEmailInEnglish() throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        notificationService.sendEmail(
            EMAIL_ADDRESS,
            templateId,
            null,
            ENGLISH
        );

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString());
    }

    @Test
    public void shouldInvokeNotificationClientToSendEmailInWelsh() throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        notificationService.sendEmail(
            EMAIL_ADDRESS,
            templateId,
            null,
            WELSH
        );

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString());
    }

    @Test
    public void shouldThrowNotificationExceptionWhenClientFailsToSendEmail()
        throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendEmail(anyString(), anyString(), eq(null), anyString());

        assertThatThrownBy(() -> notificationService.sendEmail(
            EMAIL_ADDRESS,
            templateId,
            null,
            ENGLISH
            )
        )
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");


        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString());
    }
}
