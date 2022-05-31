package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemPronounceCaseTest {

    @Mock
    private Logger logger;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ConditionalOrderPronouncedNotification notification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @InjectMocks
    private SystemPronounceCase underTest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        underTest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_PRONOUNCE_CASE);
    }

    @Test
    void shouldSendNotificationAndGenerateConditionalOrderGrantedDoc() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        underTest.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(notification, caseData, details.getId());

        verify(generateConditionalOrderPronouncedDocument).apply(details);
    }

    @Test
    void shouldNotSendNotificationAndLogErrorIfNotificationTemplateExceptionIsThrown() {

        final NotificationTemplateException notificationTemplateException = new NotificationTemplateException("Message");
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");
        doThrow(notificationTemplateException)
            .when(notificationDispatcher)
            .send(notification, caseData, details.getId());

        underTest.aboutToSubmit(details, details);

        verify(logger)
            .error("Notification failed with message: {}", "Message", notificationTemplateException);
        verify(logger)
            .info("Conditional order pronounced for Case({})", 1L);
        verifyNoMoreInteractions(logger);
    }
}
