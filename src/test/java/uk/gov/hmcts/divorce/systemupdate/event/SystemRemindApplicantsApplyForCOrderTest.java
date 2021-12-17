package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.AwaitingConditionalOrderNotification;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemindApplicantsApplyForCOrder.SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderQuestions;

@ExtendWith(SpringExtension.class)
public class SystemRemindApplicantsApplyForCOrderTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private AwaitingConditionalOrderNotification notification;

    @InjectMocks
    private SystemRemindApplicantsApplyForCOrder underTest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        underTest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_REMIND_APPLICANTS_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSendNotificationToBothApplicants() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(State.AwaitingConditionalOrder).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant1(caseData, details.getId(), true);
        verify(notification).sendToApplicant2(caseData, details.getId(), true);
        assertThat(response.getData().getApplication().getJointApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSendNotificationToApplicant1WhenAwaitingCOrderAndSole() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(State.AwaitingConditionalOrder).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant1(caseData, details.getId(), true);
        assertThat(response.getData().getApplication().getJointApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSendNotificationToApplicant2WhenCOrderPending() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(State.ConditionalOrderPending).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant2(caseData, details.getId(), true);
        assertThat(response.getData().getApplication().getJointApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSendNotificationToApplicant1WhenCOrderPending() {
        final CaseData caseData = caseData();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
            .build());
        caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setSubmittedDate(null);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(State.ConditionalOrderPending).id(1L).data(caseData)
            .build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn("auth header");

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        verify(notification).sendToApplicant1(caseData, details.getId(), true);
        assertThat(response.getData().getApplication().getJointApplicantsRemindedCanApplyForConditionalOrder()).isEqualTo(YesOrNo.YES);
    }
}
