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
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorSwitchToSoleCoNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_ANSWERS;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorSwitchToSoleCo.APPLICANT_1_SOLICITOR_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class Applicant1SolicitorSwitchToSoleCoTest {

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SolicitorSwitchToSoleCoNotification solicitorSwitchToSoleCoNotification;

    @InjectMocks
    private Applicant1SolicitorSwitchToSoleCo applicant1SolicitorSwitchToSoleCo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1SolicitorSwitchToSoleCo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_1_SOLICITOR_SWITCH_TO_SOLE_CO);
    }

    @Test
    void shouldSetApplicationTypeToSole() {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().languagePreferenceWelsh(NO).build())
            .conditionalOrder(ConditionalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorSwitchToSoleCo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getApplicationType()).isEqualTo(SOLE_APPLICATION);
        assertThat(response.getData().getApplication().getSwitchedToSoleCo()).isEqualTo(YES);
        assertThat(response.getData().getLabelContent().getApplicant2()).isEqualTo("respondent");
        assertThat(response.getData().getConditionalOrder().getSwitchedToSole()).isEqualTo(YES);

        verify(documentGenerator).generateAndStoreCaseDocument(
            eq(CONDITIONAL_ORDER_ANSWERS),
            eq(CONDITIONAL_ORDER_ANSWERS_TEMPLATE_ID),
            eq(CONDITIONAL_ORDER_ANSWERS_DOCUMENT_NAME),
            any(),
            anyLong(),
            eq(caseData.getApplicant1()));
    }

    @Test
    void shouldSendEmailsInSubmittedCallback() {
        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        applicant1SolicitorSwitchToSoleCo.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(solicitorSwitchToSoleCoNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldKeepSameStateIfInJSAwaitingLA() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .state(JSAwaitingLA)
            .build();

        var response = applicant1SolicitorSwitchToSoleCo.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getState()).isEqualTo(JSAwaitingLA);
    }

    @Test
    void shouldProgressStateIfNotInStateJSAwaitingLA() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .state(ConditionalOrderPending)
            .build();

        var response = applicant1SolicitorSwitchToSoleCo.aboutToSubmit(caseDetails, caseDetails);
        assertThat(response.getState()).isEqualTo(AwaitingLegalAdvisorReferral);
    }
}
