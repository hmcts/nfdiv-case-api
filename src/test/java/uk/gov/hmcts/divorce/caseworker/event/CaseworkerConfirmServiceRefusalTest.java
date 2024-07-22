package uk.gov.hmcts.divorce.caseworker.event;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerConfirmServiceRefusal.CASEWORKER_CONFIRM_SERVICE_REFUSAL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerConfirmServiceRefusalTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ServiceApplicationNotification serviceApplicationNotification;

    @InjectMocks
    private CaseworkerConfirmServiceRefusal caseworkerConfirmServiceRefusal;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerConfirmServiceRefusal.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getId)
                .contains(CASEWORKER_CONFIRM_SERVICE_REFUSAL);
    }

    @Test
    void shouldSendNotificationsWhenSubmittedCallback() {
        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
                .data(CaseData.builder()
                        .alternativeService(AlternativeService
                                .builder()
                                .build())
                        .build())
                .build();
        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder().build();

        caseworkerConfirmServiceRefusal.submitted(details, beforeDetails);

        verify(notificationDispatcher).send(serviceApplicationNotification, details.getData(), details.getId());
    }
}
