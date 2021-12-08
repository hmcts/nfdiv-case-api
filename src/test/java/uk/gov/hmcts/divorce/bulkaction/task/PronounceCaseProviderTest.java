package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;

@ExtendWith(MockitoExtension.class)
class PronounceCaseProviderTest {

    @InjectMocks
    private PronounceCaseProvider pronounceCaseProvider;

    @Test
    void shouldReturnSystemPronounceCaseEventId() {
        assertThat(pronounceCaseProvider.getEventId()).isEqualTo(SYSTEM_PRONOUNCE_CASE);
    }

    @Test
    void shouldReturnSystemPronounceCaseTask() {

        final var localDateTime = getExpectedLocalDateTime();
        final var localDate = getExpectedLocalDate();
        final var expectedDate = FinalOrder.builder().build().getDateFinalOrderEligibleFrom(localDateTime);
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(localDateTime)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setData(bulkActionCaseData);

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = pronounceCaseProvider.getCaseTask(bulkCaseDetails);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final CaseData resultCaseData = resultCaseDetails.getData();
        final ConditionalOrder resultConditionalOrder = resultCaseData.getConditionalOrder();

        assertThat(resultCaseData.getDueDate()).isEqualTo(expectedDate);
        assertThat(resultConditionalOrder.getOutcomeCase()).isEqualTo(YES);
        assertThat(resultConditionalOrder.getGrantedDate()).isEqualTo(localDate);
        assertThat(resultCaseData.getFinalOrder().getDateFinalOrderEligibleFrom()).isEqualTo(expectedDate);
    }
}