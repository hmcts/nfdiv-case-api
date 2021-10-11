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
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeServiceDecision.LEGAL_ADVISOR_SERVICE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMakeServiceDecisionTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private LegalAdvisorMakeServiceDecision makeServiceDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        makeServiceDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_SERVICE_DECISION);
    }

    @Test
    void shouldUpdateStateToHoldingAndSetServiceApplicationDecisionDateIfServiceApplicationIsGranted() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(YES)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(Holding);
    }

    @Test
    void shouldNotUpdateStateAndSetServiceApplicationDecisionDateIfServiceApplicationIsNotGranted() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(NO)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AwaitingServiceConsideration);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isNull();

        assertThat(response.getState()).isEqualTo(AwaitingServiceConsideration);
    }
}
