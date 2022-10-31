package uk.gov.hmcts.divorce.solicitor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;

@ExtendWith(MockitoExtension.class)
public class SolicitorIntendsToSwitchToSoleFoNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SolicitorIntendsToSwitchToSoleFoNotification solicitorIntendsToSwitchToSoleFoNotification;

    @Test
    void shouldSendApplicant1SolicitorNotificationIfApplicantIsRepresented() {

    }
}
