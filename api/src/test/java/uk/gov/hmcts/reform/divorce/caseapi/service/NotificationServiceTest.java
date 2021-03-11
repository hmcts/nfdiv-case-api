package uk.gov.hmcts.reform.divorce.caseapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.divorce.caseapi.exceptions.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.LanguagePreference.WELSH;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class NotificationServiceTest {

    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private NotificationService notificationService;

    @Mock
    private SendEmailResponse sendEmailResponse;

    @Test
    void shouldInvokeNotificationClientToSendEmailInEnglish() throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());

        doReturn(sendEmailResponse)
            .when(notificationClient)
            .sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                isNull(),
                anyString()
            );

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

        verify(sendEmailResponse).getReference();
        verify(sendEmailResponse).getNotificationId();

        verifyNoMoreInteractions(notificationClient, sendEmailResponse);
    }

    @Test
    void shouldInvokeNotificationClientToSendEmailInWelsh() throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());

        doReturn(sendEmailResponse)
            .when(notificationClient)
            .sendEmail(
                eq(templateId),
                eq(EMAIL_ADDRESS),
                isNull(),
                anyString()
            );

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

        verify(sendEmailResponse).getReference();
        verify(sendEmailResponse).getNotificationId();

        verifyNoMoreInteractions(notificationClient, sendEmailResponse);
    }

    @Test
    void shouldThrowNotificationExceptionWhenClientFailsToSendEmail()
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
