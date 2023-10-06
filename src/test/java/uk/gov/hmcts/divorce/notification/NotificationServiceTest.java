package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.notification.exception.NotificationException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SAVE_SIGN_OUT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final String EMAIL_ADDRESS = "simulate-delivered@notifications.service.gov.uk";

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private SendEmailResponse sendEmailResponse;

    final String replyToId = UUID.randomUUID().toString();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(notificationService, "replyToId", replyToId);
    }

    @Test
    void shouldInvokeNotificationClientToSendEmailInEnglish() throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), templateId)
            ));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString(),
            any()
        )).thenReturn(sendEmailResponse);

        notificationService.sendEmail(
            EMAIL_ADDRESS,
            SAVE_SIGN_OUT,
            null,
            ENGLISH,
            TEST_CASE_ID
        );

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString(),
            eq(replyToId));

        verify(sendEmailResponse).getReference();
        verify(sendEmailResponse).getNotificationId();

        verifyNoMoreInteractions(notificationClient, sendEmailResponse);
    }

    @Test
    void shouldInvokeNotificationClientToSendEmailInWelsh() throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        when(sendEmailResponse.getReference()).thenReturn(Optional.of(randomUUID().toString()));
        when(sendEmailResponse.getNotificationId()).thenReturn(UUID.randomUUID());
        when(emailTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                WELSH, Map.of(SAVE_SIGN_OUT.name(), templateId)
            ));

        when(notificationClient.sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString(),
            any()
        )).thenReturn(sendEmailResponse);

        notificationService.sendEmail(
            EMAIL_ADDRESS,
            SAVE_SIGN_OUT,
            null,
            WELSH,
            TEST_CASE_ID
        );

        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString(),
            eq(replyToId));

        verify(sendEmailResponse).getReference();
        verify(sendEmailResponse).getNotificationId();

        verifyNoMoreInteractions(notificationClient, sendEmailResponse);
    }

    @Test
    void shouldThrowNotificationExceptionWhenClientFailsToSendEmail()
        throws NotificationClientException {
        String templateId = UUID.randomUUID().toString();

        doThrow(new NotificationClientException("some message"))
            .when(notificationClient).sendEmail(anyString(), anyString(), eq(null), anyString(), any());

        when(emailTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(SAVE_SIGN_OUT.name(), templateId)
            ));

        assertThatThrownBy(() -> notificationService.sendEmail(
            EMAIL_ADDRESS,
            SAVE_SIGN_OUT,
            null,
            ENGLISH,
            TEST_CASE_ID
            )
        )
            .isInstanceOf(NotificationException.class)
            .hasMessageContaining("some message");


        verify(notificationClient).sendEmail(
            eq(templateId),
            eq(EMAIL_ADDRESS),
            isNull(),
            anyString(),
            eq(replyToId));
    }
}
