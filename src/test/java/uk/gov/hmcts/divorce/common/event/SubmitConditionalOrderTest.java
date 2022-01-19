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
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.event.SubmitConditionalOrder.SUBMIT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SubmitConditionalOrderTest {

    private static final String DUMMY_AUTH_TOKEN = "ASAFSDFASDFASDFASDFASDF";
    private static final String DUMMY_USER_ID = "123123123123";

    @Mock
    private AppliedForConditionalOrderNotification appliedForConditionalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private Clock clock;

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest request;

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
        setupMocks();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData()).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getSubmittedDate())
            .isEqualTo(getExpectedLocalDateTime());
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate())
            .isNull();
    }

    @Test
    void shouldSetDateSubmittingUserIdOnAboutToSubmit() {
        setupMocks();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData()).build();

        submitConditionalOrder.aboutToSubmit(caseDetails, null);

        verify(appliedForConditionalOrderNotification).setSubmittingUserId(DUMMY_USER_ID);
    }

    @Test
    void shouldSendEmailOnAboutToSubmitIfApplicantIsNotRepresented() {
        setupMocks();
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

    private void setupMocks() {
        setMockClock(clock);
        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(idamService.retrieveUser(DUMMY_AUTH_TOKEN))
            .thenReturn(new User(DUMMY_AUTH_TOKEN, UserDetails.builder().id(DUMMY_USER_ID).build()));
    }
}
