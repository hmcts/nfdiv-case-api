package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.common.event.SubmitConditionalOrder.SUBMIT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SubmitConditionalOrderTest {

    @Mock
    private AppliedForConditionalOrderNotification notification;

    @Mock
    private Clock clock;

    @InjectMocks
    private SubmitConditionalOrder submitConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        submitConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUBMIT_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSetDateSubmittedOnAboutToSubmit() {
        setMockClock(clock);
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getApplicant1SubmittedDate()).isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSendEmailOnAboutToSubmitIfApplicantIsNotRepresented() {
        setMockClock(clock);
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        submitConditionalOrder.aboutToSubmit(caseDetails, null);

        verify(notification).sendToApplicant1(caseData, 1L);
    }
}
