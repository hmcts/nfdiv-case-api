package uk.gov.hmcts.divorce.caseworker.event;

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

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerMakeBailiffDecision.CASEWORKER_BAILIFF_DECISION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerMakeBailiffDecisionTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerMakeBailiffDecision makeBailiffDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        makeBailiffDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_BAILIFF_DECISION);
    }

    @Test
    void shouldChangeCaseStateToAwaitingBailiffServiceAndSetDecisionDateWhenServiceApplicationIsGrantedAndServiceTypeIsBailiff() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(YES);
        caseData.getAlternativeService().setServiceApplicationType(BAILIFF);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingBailiffService);
        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());
    }

    @Test
    void shouldChangeCaseStateToAwaitingConditionalOrderAndSetDecisionDateWhenServiceApplicationIsGrantedAndServiceTypeIsNotBailiff() {
        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(YES);
        caseData.getAlternativeService().setServiceApplicationType(DEEMED);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingConditionalOrder);
        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());
    }

    @Test
    void shouldChangeCaseStateToAwaitingAosWhenServiceApplicationIsNotGranted() {
        final CaseData caseData = caseData();
        caseData.getAlternativeService().setServiceApplicationGranted(NO);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> aboutToStartOrSubmitResponse =
            makeBailiffDecision.aboutToSubmit(details, details);

        assertThat(aboutToStartOrSubmitResponse.getState()).isEqualTo(AwaitingAos);
        assertThat(aboutToStartOrSubmitResponse.getData().getAlternativeService().getServiceApplicationDecisionDate())
            .isNull();
    }
}
