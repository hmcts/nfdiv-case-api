package uk.gov.hmcts.divorce.solicitor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.service.task.GenerateConditionalOrderAnswersDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorSwitchToSoleCoNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorSwitchToSoleCo.APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class Applicant2SolicitorSwitchToSoleCoTest {

    @Mock
    private SwitchToSoleService switchToSoleService;

    @Mock
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SolicitorSwitchToSoleCoNotification solicitorSwitchToSoleCoNotification;

    @InjectMocks
    private Applicant2SolicitorSwitchToSoleCo applicant2SolicitorSwitchToSoleCo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorSwitchToSoleCo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_SOLICITOR_SWITCH_TO_SOLE_CO);
    }

    @Test
    void shouldSetApplicationTypeToSoleAndSwitchCitizenAndSolicitorRoles() {
        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorSwitchToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getConditionalOrder().getSwitchedToSole()).isEqualTo(YES);

        verify(switchToSoleService).switchUserRoles(caseData, TEST_CASE_ID);
        verify(generateConditionalOrderAnswersDocument).apply(eq(caseDetails), any());
    }

    @Test
    void shouldSendEmailsInSubmittedCallback() {
        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        applicant2SolicitorSwitchToSoleCo.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(solicitorSwitchToSoleCoNotification, caseData, TEST_CASE_ID);
    }
}
