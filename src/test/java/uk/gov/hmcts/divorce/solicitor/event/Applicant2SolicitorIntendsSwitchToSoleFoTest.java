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

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorIntendsSwitchToSoleFo.APPLICANT_2_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class Applicant2SolicitorIntendsSwitchToSoleFoTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant2SolicitorIntendsSwitchToSoleFo applicant2SolicitorIntendsSwitchToSoleFo;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorIntendsSwitchToSoleFo.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_INTENDS_TO_SWITCH_TO_SOLE_FO);
    }

    @Test
    void shouldSetIntendSwitchToSoleFinalOrderFields() {

        setMockClock(clock);

        CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorIntendsSwitchToSoleFo.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getFinalOrder().getDoesApplicant2IntendToSwitchToSole())
            .isEqualTo(YES);
        assertThat(response.getData().getFinalOrder().getDateApplicant2DeclaredIntentionToSwitchToSoleFo())
            .isEqualTo(LocalDate.now(clock));
    }
}
