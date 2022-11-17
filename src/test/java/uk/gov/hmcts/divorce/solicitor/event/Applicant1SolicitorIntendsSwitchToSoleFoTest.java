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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorIntendsToSwitchToSoleFoNotification;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinalOrder.IntendsToSwitchToSole.I_INTEND_TO_SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorIntendsSwitchToSoleFo.APPLICANT_1_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorIntendsSwitchToSoleFo.INTEND_TO_SWITCHED_TO_SOLE_FO_ERROR;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class Applicant1SolicitorIntendsSwitchToSoleFoTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SolicitorIntendsToSwitchToSoleFoNotification solicitorIntendsToSwitchToSoleFoNotification;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant1SolicitorIntendsSwitchToSoleFo applicant1SolicitorIntendsSwitchToSoleFo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant1SolicitorIntendsSwitchToSoleFo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_1_INTENDS_TO_SWITCH_TO_SOLE_FO);
    }

    @Test
    void shouldReturnErrorIfCheckboxNotChecked() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorIntendsSwitchToSoleFo.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains(INTEND_TO_SWITCHED_TO_SOLE_FO_ERROR);
    }

    @Test
    void shouldNotReturnErrorIfCheckboxChecked() {

        final CaseData caseData = caseData();
        caseData.getFinalOrder().setApplicant1IntendsToSwitchToSole(Set.of(I_INTEND_TO_SWITCH_TO_SOLE));

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorIntendsSwitchToSoleFo.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSetIntendSwitchToSoleFinalOrderFields() {

        setMockClock(clock);

        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant1SolicitorIntendsSwitchToSoleFo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getFinalOrder().getDoesApplicant1IntendToSwitchToSole())
            .isEqualTo(YES);
        assertThat(response.getData().getFinalOrder().getDateApplicant1DeclaredIntentionToSwitchToSoleFo())
            .isEqualTo(LocalDate.now(clock));
    }

    @Test
    void shouldTriggerEmailInSubmittedCallback() {

        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        applicant1SolicitorIntendsSwitchToSoleFo.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(solicitorIntendsToSwitchToSoleFoNotification, caseData, TEST_CASE_ID);
    }
}
