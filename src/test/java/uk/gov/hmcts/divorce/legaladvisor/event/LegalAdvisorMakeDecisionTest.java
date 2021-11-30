package uk.gov.hmcts.divorce.legaladvisor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorClarificationSubmittedNotification;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMakeDecisionTest {

    @Mock
    private LegalAdvisorClarificationSubmittedNotification notification;

    @Mock
    private Clock clock;

    @InjectMocks
    private LegalAdvisorMakeDecision legalAdvisorMakeDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        legalAdvisorMakeDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_MAKE_DECISION);
    }

    @Test
    void shouldSetGrantedDateAndStateToAwaitingPronouncementIfConditionalOrderIsGranted() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(response.getState()).isEqualTo(AwaitingPronouncement);
    }

    @Test
    void shouldSetStateToAwaitingClarificationIfConditionalOrderIsNotGranted() {

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingClarification);
    }

    @Test
    void shouldSendEmailInSubmittedCallback() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder().solSignStatementOfTruth(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingClarification);
        caseDetails.setId(12345L);

        legalAdvisorMakeDecision.submitted(caseDetails, caseDetails);

        verify(notification).send(caseData, 12345L);
    }

    @Test
    void shouldNotSendEmailInSubmittedCallbackIfNotSolicitorApplication() {
        final CaseData caseData = CaseData.builder()
            .application(Application.builder().solSignStatementOfTruth(NO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingClarification);

        legalAdvisorMakeDecision.submitted(caseDetails, caseDetails);

        verifyNoInteractions(notification);
    }

    @Test
    void shouldNotSendEmailInSubmittedCallbackIfNotInAwaitingClarificationState() {
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPronouncement);

        legalAdvisorMakeDecision.submitted(caseDetails, caseDetails);

        verifyNoInteractions(notification);
    }
}
