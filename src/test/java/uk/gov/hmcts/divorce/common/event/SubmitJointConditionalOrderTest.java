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
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorAppliedForConditionalOrderNotification;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.event.SubmitJointConditionalOrder.SUBMIT_JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class SubmitJointConditionalOrderTest {

    @Mock
    private Clock clock;

    @Mock
    private SolicitorAppliedForConditionalOrderNotification solicitorAppliedForConditionalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private Applicant2AppliedForConditionalOrderNotification app2AppliedForConditionalOrderNotification;

    @Mock
    private DocumentGenerator documentGenerator;

    @InjectMocks
    private SubmitJointConditionalOrder submitJointConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        submitJointConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUBMIT_JOINT_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSetDateSubmittedOnAboutToSubmit() {
        setMockClock(clock);
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YesOrNo.YES)
                    .build())
                .build())
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails =
            CaseDetails.<CaseData, State>builder()
                .data(caseData)
                .id(
                    TEST_CASE_ID)
                .state(ConditionalOrderPending)
                .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitJointConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate())
            .isEqualTo(getExpectedLocalDateTime());
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted())
            .isEqualTo(YesOrNo.YES);

        verify(notificationDispatcher, times(0))
            .send(app2AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendApp1SolicitorAndApp2SolicitorNotificationsOnSubmittedCallback() {
        CaseData caseData = caseData();
        caseData.setApplicant1(Applicant
            .builder()
            .solicitorRepresented(YesOrNo.YES)
            .solicitor(Solicitor
                .builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build())
            .build());
        caseData.setApplicant2(Applicant
            .builder()
            .solicitorRepresented(YesOrNo.YES)
            .solicitor(Solicitor
                .builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build())
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .state(AwaitingLegalAdvisorReferral)
            .build();

        final CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).build();

        submitJointConditionalOrder.submitted(caseDetails, beforeDetails);

        verify(notificationDispatcher).send(solicitorAppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSetStateToConditionalOrderPendingOnAboutToSubmit() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.ConditionalOrderDrafted).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitJointConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ConditionalOrderPending);

        verifyNoInteractions(documentGenerator);
    }

    @Test
    void shouldSendApplicant2NotificationWhenConditionalOrderPendingOnSubmittedCallback() {

        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.ConditionalOrderPending).id(TEST_CASE_ID).build();

        submitJointConditionalOrder.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(app2AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSetStateToLegalAdvisorReferralAndGenerateConditionalOrderAnswersDocumentOnAboutToSubmit() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.ConditionalOrderPending).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitJointConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);

        verify(documentGenerator).generateAndStoreCaseDocument(
            eq(CONDITIONAL_ORDER_ANSWERS),
            eq(CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID),
            eq(CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME),
            any(),
            anyLong(),
            eq(caseData.getApplicant2()));

        verify(notificationDispatcher, times(0))
            .send(app2AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }
}
