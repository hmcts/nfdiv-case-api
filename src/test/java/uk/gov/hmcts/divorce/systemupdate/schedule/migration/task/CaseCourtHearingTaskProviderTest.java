package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

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
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCaseWithCoEGeneration.SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;

@ExtendWith(MockitoExtension.class)
class CaseCourtHearingTaskProviderTest {

    @InjectMocks
    private CaseCourtHearingTaskProvider caseCourtHearingTaskProvider;

    @Test
    void shouldReturnSystemUpdateCaseCourtHearingEventId() {
        assertThat(caseCourtHearingTaskProvider.getEventId()).isEqualTo(SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION);
    }

    @Test
    void shouldSetCourtHearingDateAndCourt() {

        final var localDateTime = getExpectedLocalDateTime();
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(localDateTime)
            .court(BIRMINGHAM)
            .build();

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setData(bulkActionCaseData);

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = caseCourtHearingTaskProvider.getCaseTask(bulkCaseDetails);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final ConditionalOrder resultConditionalOrder = resultCaseDetails.getData().getConditionalOrder();

        assertThat(resultConditionalOrder.getDateAndTimeOfHearing()).isEqualTo(localDateTime);
        assertThat(resultConditionalOrder.getCourt()).isEqualTo(BIRMINGHAM);
    }
}