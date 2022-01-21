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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.APPROVE;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorGeneralConsideration.LEGAL_ADVISOR_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorGeneralConsiderationTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private LegalAdvisorGeneralConsideration legalAdvisorGeneralConsideration;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        legalAdvisorGeneralConsideration.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_GENERAL_CONSIDERATION);
    }

    @Test
    void shouldSetGeneralReferralDecisionDateWhenAboutToSubmitIsInvoked() {

        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .generalReferral(
                GeneralReferral
                    .builder()
                    .generalReferralDecision(APPROVE)
                    .generalReferralDecisionReason("approved")
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorGeneralConsideration.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getGeneralReferral().getGeneralReferralDecisionDate()).isEqualTo(getExpectedLocalDate());
    }
}
