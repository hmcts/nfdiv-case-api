package uk.gov.hmcts.divorce.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.citizen.event.CitizenFinalOrderRequested.CITIZEN_FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForAwaitingFinalOrder;

@ExtendWith(MockitoExtension.class)
class CitizenFinalOrderRequestedTest {

    @InjectMocks
    private CitizenFinalOrderRequested citizenFinalOrderRequested;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        citizenFinalOrderRequested.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_FINAL_ORDER_REQUESTED);
    }

    @Test
    void shouldSetStateToFinalOrderOverdueIfDateFinalOrderNoLongerEligibleIsPast() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.getFinalOrder().setDateFinalOrderNoLongerEligible(getExpectedLocalDate().minusWeeks(1L));

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderRequested.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.FinalOrderOverdue);
    }

    @Test
    void shouldSetStateToFinalOrderRequestedIfDateFinalOrderNoLongerEligibleIsPast() {
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validCaseDataForAwaitingFinalOrder();
        caseData.getFinalOrder().setDateFinalOrderNoLongerEligible(getExpectedLocalDate().plusWeeks(1L));

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = citizenFinalOrderRequested.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.FinalOrderRequested);
    }
}
