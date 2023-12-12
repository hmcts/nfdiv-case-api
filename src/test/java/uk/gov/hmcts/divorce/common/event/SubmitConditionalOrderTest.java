package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorAppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitConditionalOrder.SUBMIT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;

@ExtendWith(MockitoExtension.class)
class SubmitConditionalOrderTest {

    private static final String DUMMY_AUTH_TOKEN = "ASAFSDFASDFASDFASDFASDF";

    @Mock
    private Applicant1AppliedForConditionalOrderNotification app1AppliedForConditionalOrderNotification;

    @Mock
    private Applicant2AppliedForConditionalOrderNotification app2AppliedForConditionalOrderNotification;

    @Mock
    private SolicitorAppliedForConditionalOrderNotification solicitorAppliedForConditionalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Clock clock;

    @Mock
    private DocumentGenerator documentGenerator;

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
    void shouldSetDateSubmittedForApplicant1OnAboutToSubmit() {
        setupMocks(clock);
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .applicationType(SOLE_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getSubmittedDate())
            .isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldFailValidationWhenApplicant1NotConfirmedStatementOfTruthOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .build())
                .build())
            .applicationType(SOLE_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, null);

        assertThat(response.getErrors()).contains("The applicant must agree that the facts stated in the application are true");
    }

    @Test
    void shouldSetDateSubmittedForApplicant2OnAboutToSubmit() {
        setupMocks(clock);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .applicationType(JOINT_APPLICATION)
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getSubmittedDate())
            .isEqualTo(getExpectedLocalDateTime());
    }

    @Test
    void shouldSetStateToConditionalOrderPendingOnAboutToSubmit() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderDrafted).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ConditionalOrderPending);

        verifyNoInteractions(documentGenerator);
    }

    @Test
    void shouldSetStateToAwaitingLegalAdvisorReferralIfJointApplicationOnAboutToSubmit() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderPending).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);

        verify(documentGenerator).generateAndStoreCaseDocument(
            eq(CONDITIONAL_ORDER_ANSWERS),
            eq(CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID),
            eq(CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME),
            any(),
            anyLong(),
            eq(caseData.getApplicant1()));
    }

    @Test
    void shouldSetStateToConditionalOrderPendingIfJointApplicationAndOnlyOneCODatePresentOnAboutToSubmit() {
        setupMocks(clock);
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).build())
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderPending).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ConditionalOrderPending);
    }

    @Test
    void shouldSetStateToAwaitingLegalAdvisorReferralIfSoleApplicationAndOnAboutToSubmit() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(NO);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(AwaitingConditionalOrder).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);

        verify(documentGenerator).generateAndStoreCaseDocument(eq(CONDITIONAL_ORDER_ANSWERS),
            eq(CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID),
            eq(CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME),
            any(),
            anyLong(),
            eq(caseData.getApplicant1()));
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfSoleApplicationAndapp1LanguagePreferenceWelshIsYes() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(AwaitingConditionalOrder).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingLegalAdvisorReferral);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfJointApplicationAndapp1LanguagePreferenceWelshIsYesAndCoSubmitted() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderPending).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingLegalAdvisorReferral);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfJointApplicationAndApp2LanguagePreferenceWelshIsYesAndCoSubmitted() {
        setupMocks(null);
        final CaseData caseData = CaseData.builder().applicationType(JOINT_APPLICATION).build();
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                .statementOfTruth(YES).submittedDate(getExpectedLocalDateTime()).build())
            .build());
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderPending).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(AwaitingLegalAdvisorReferral);
    }

    @Test
    void shouldSendApp1NotificationsOnAboutToSubmit() {
        setupMocks(clock);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);

        CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        submitConditionalOrder.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(app1AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendApp2NotificationsOnAboutToSubmit() {
        setupMocks(clock);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);

        CaseData caseData = caseData();
        caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().setStatementOfTruth(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        submitConditionalOrder.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(app2AppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSendApp1SolicitorAndApp2SolicitorNotificationsOnAboutToSubmit() {
        setupMocks(clock);
        CaseData caseData = caseData();
        caseData.setApplicant1(Applicant
            .builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor
                .builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build())
            .build());
        caseData.setApplicant2(Applicant
            .builder()
            .solicitorRepresented(YES)
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

        CaseData caseDataBefore = caseData();

        final CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseDataBefore)
            .state(ConditionalOrderPronounced)
            .build();

        submitConditionalOrder.aboutToSubmit(caseDetails, beforeDetails);

        verify(notificationDispatcher).send(solicitorAppliedForConditionalOrderNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldSetIsSubmittedForApplicant1OnAboutToSubmit() {
        setupMocks(clock);
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .application(Application.builder()
                .serviceMethod(SOLICITOR_SERVICE)
                .solSignStatementOfTruth(YES)
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderDrafted).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted())
            .isEqualTo(YES);
    }

    @Test
    void shouldSetIsSubmittedForApplicant2OnAboutToSubmit() {
        setupMocks(clock);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(false);
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .application(Application.builder()
                .serviceMethod(SOLICITOR_SERVICE)
                .solSignStatementOfTruth(YES)
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(ConditionalOrderDrafted).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted())
            .isEqualTo(YES);
    }

    @Test
    void shouldSetIsApplicant2ToOfflineIfTheyAreNotLinkedAndNotSubmittedAosAndSuccessfulBailiffApplication() {
        setupMocks(clock);
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().build());
        caseData.getConditionalOrder().setLastApprovedServiceApplicationIsBailiffApplication(YES);
        caseData.setCaseInvite(CaseInvite.builder().accessCode("ACCESS12").build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldSetIsApplicant2ToOfflineIfTheyAreNotLinkedAndNotSubmittedAosAndServiceConfirmed() {
        setupMocks(clock);
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().build());
        caseData.getConditionalOrder().setServiceConfirmed(YES);
        caseData.setCaseInvite(CaseInvite.builder().accessCode("ACCESS12").build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldSetIsApplicant2ToOfflineIfTheyAreNotLinkedAndNotSubmittedAosAndDeemedApplicationSuccessful() {
        setupMocks(clock);
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().build());
        caseData.getAlternativeService().setServiceApplicationGranted(YES);
        caseData.getAlternativeService().setAlternativeServiceType(DEEMED);
        caseData.setCaseInvite(CaseInvite.builder().accessCode("ACCESS12").build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldSetIsApplicant2ToOfflineIfTheyAreNotLinkedAndNotSubmittedAosAndDispensedApplicationSuccessful() {
        setupMocks(clock);
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(AcknowledgementOfService.builder().build());
        caseData.getAlternativeService().setServiceApplicationGranted(YES);
        caseData.getAlternativeService().setAlternativeServiceType(DISPENSED);
        caseData.setCaseInvite(CaseInvite.builder().accessCode("ACCESS12").build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().isApplicantOffline()).isTrue();
    }

    @Test
    void shouldNotSetIsApplicant2ToOfflineOnAboutToSubmitIfLinkedAndSubmittedAos() {
        setupMocks(clock);
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder()
                .dateAosSubmitted(LocalDateTime.now(clock))
                .build()
        );
        caseData.setCaseInvite(CaseInvite.builder().build());
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicant2().isApplicantOffline()).isFalse();
    }

    private CaseData caseData() {
        return CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().statementOfTruth(YES).build())
                .build())
            .applicationType(SOLE_APPLICATION)
            .build();
    }

    private void setupMocks(Clock mockClock) {
        if (Objects.nonNull(mockClock)) {
            setMockClock(mockClock);
        }
        when(request.getHeader(eq(AUTHORIZATION))).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(true);
    }
}
