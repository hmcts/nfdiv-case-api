package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.notification.SoleAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.citizen.event.CitizenFinalOrderDelayReason.CITIZEN_FINAL_ORDER_DELAY_REASON;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenFinalOrderDelayReasonTest {

    @Mock
    private SoleAppliedForFinalOrderNotification soleAppliedForFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private CitizenFinalOrderDelayReason citizenFinalOrderDelayReason;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenFinalOrderDelayReason.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_FINAL_ORDER_DELAY_REASON);
    }

    @Test
    void shouldChangeStateToFinalOrderRequestedAndSendNotificationsOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderDelayReason.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(FinalOrderRequested);

        verify(notificationDispatcher).send(soleAppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
