package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateFinalOrderFieldsTest {

    @InjectMocks
    private CaseworkerUpdateFinalOrderFields caseworkerUpdateFinalOrderFields;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateFinalOrderFields.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CaseworkerUpdateFinalOrderFields.CASEWORKER_UPDATE_FINAL_ORDER_FIELDS);
    }

    @Test
    void shouldSetFinalOrderFieldsWhenAboutToSubmitCallbackIsInvoked() {
        final var localDateTime = getExpectedLocalDateTime();
        final var expectedGrantedDate = getExpectedLocalDate();
        final var expectedFinalOrderEligibleFrom = FinalOrder.builder().build().getDateFinalOrderEligibleFrom(localDateTime);
        final var expectedFinalOrderEligibleToRespondent = expectedFinalOrderEligibleFrom.plusMonths(3L);
        final var expectedFinalOrderNoLongerEligible = expectedGrantedDate.plusMonths(12L);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(
            CaseData
                .builder()
                .conditionalOrder(
                    ConditionalOrder
                        .builder()
                        .dateAndTimeOfHearing(localDateTime)
                        .build()
                )
                .build()
        );
        caseDetails.setId(12345L);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateFinalOrderFields.aboutToSubmit(caseDetails, caseDetails);

        final ConditionalOrder resultConditionalOrder = response.getData().getConditionalOrder();

        assertThat(response.getData().getDueDate()).isEqualTo(expectedFinalOrderEligibleFrom);
        assertThat(resultConditionalOrder.getOutcomeCase()).isEqualTo(YES);
        assertThat(resultConditionalOrder.getGrantedDate()).isEqualTo(expectedGrantedDate);

        final FinalOrder resultFinalOrder = response.getData().getFinalOrder();
        assertThat(resultFinalOrder.getDateFinalOrderEligibleFrom()).isEqualTo(expectedFinalOrderEligibleFrom);
        assertThat(resultFinalOrder.getDateFinalOrderNoLongerEligible()).isEqualTo(expectedFinalOrderNoLongerEligible);
        assertThat(resultFinalOrder.getDateFinalOrderEligibleToRespondent()).isEqualTo(expectedFinalOrderEligibleToRespondent);

    }
}
