package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.common.notification.Applicant2AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenFinalOrderDelayReason.CITIZEN_FINAL_ORDER_DELAY_REASON;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CitizenFinalOrderDelayReasonTest {

    private static final String DUMMY_AUTH_TOKEN = "ASAFSDFASDFASDFASDFASDF";

    @Mock
    private Applicant1AppliedForFinalOrderNotification applicant1AppliedForFinalOrderNotification;

    @Mock
    private Applicant2AppliedForFinalOrderNotification applicant2AppliedForFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest request;

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
    void shouldChangeStateToFinalOrderRequestedOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderDelayReason.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(FinalOrderRequested);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfSoleAndApp1LanguagePreferenceWelshYes() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderDelayReason.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(FinalOrderRequested);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfJointAndApp1LanguagePreferenceWelshYes() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderDelayReason.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(FinalOrderRequested);
    }

    @Test
    void shouldSetStateToWelshTranslationReviewIfJointAndApp2LanguagePreferenceWelshYes() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.JOINT_APPLICATION).build();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderDelayReason.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(WelshTranslationReview);
        assertThat(response.getData().getApplication().getWelshPreviousState()).isEqualTo(FinalOrderRequested);
    }

    @Test
    void shouldSendApplicant1NotificationInSubmittedCallback() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(true);

        citizenFinalOrderDelayReason.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant1AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendApplicant2NotificationInSubmittedCallback() {
        final CaseData caseData = CaseData.builder().applicationType(ApplicationType.SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(State.FinalOrderOverdue).id(1L).build();

        when(request.getHeader(AUTHORIZATION)).thenReturn(DUMMY_AUTH_TOKEN);
        when(ccdAccessService.isApplicant1(DUMMY_AUTH_TOKEN, 1L)).thenReturn(false);

        citizenFinalOrderDelayReason.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2AppliedForFinalOrderNotification, caseData, caseDetails.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
