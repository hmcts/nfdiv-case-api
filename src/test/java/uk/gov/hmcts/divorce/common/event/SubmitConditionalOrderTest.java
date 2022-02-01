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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.SubmitConditionalOrder.SUBMIT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SubmitConditionalOrderTest {

    private static final String DUMMY_AUTH_TOKEN = "ASAFSDFASDFASDFASDFASDF";

    @Mock
    private AppliedForConditionalOrderNotification appliedForConditionalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

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
        setupMocks(clock);
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES)
                    .build())
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES)
                    .build())
                .build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getSubmittedDate())
            .isEqualTo(getExpectedLocalDateTime());
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate())
            .isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSetStateOnAboutToSubmit() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.ConditionalOrderDrafted).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ConditionalOrderPending);
    }

    @Test
    void shouldSetSubmittingUserOnAboutToSubmit() {
        setupMocks(clock);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData()).id(1L).build();

        submitConditionalOrder.aboutToSubmit(caseDetails, null);

        verify(appliedForConditionalOrderNotification).setSubmittingUser(true);
    }

    @Test
    void shouldSendEmailOnAboutToSubmitIfApplicantIsNotRepresented() {
        setupMocks(clock);
        CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(1L).data(caseData).build();

        submitConditionalOrder.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(appliedForConditionalOrderNotification, caseData, 1L);
    }

    private CaseData caseData() {
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().statementOfTruth(YesOrNo.YES).build())
                .build())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .build();
        return caseData;
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L))
            .thenReturn(true);
    }
}
