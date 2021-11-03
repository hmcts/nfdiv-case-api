package uk.gov.hmcts.divorce.bulkaction.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.BulkCaseCaseTaskFactory;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.Court.SERVICE_CENTRE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;

@ExtendWith(MockitoExtension.class)
class BulkCaseCaseTaskFactoryTest {

    @InjectMocks
    private BulkCaseCaseTaskFactory bulkCaseCaseTaskFactory;

    @Test
    void shouldReturnSystemUpdateCaseCourtHearingCaseTask() {

        final var localDateTime = getExpectedLocalDateTime();
        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(localDateTime)
            .courtName(SERVICE_CENTRE)
            .build();

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_UPDATE_CASE_COURT_HEARING);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final ConditionalOrder resultConditionalOrder = resultCaseDetails.getData().getConditionalOrder();

        assertThat(resultConditionalOrder.getDateAndTimeOfHearing()).isEqualTo(localDateTime);
        assertThat(resultConditionalOrder.getCourtName()).isEqualTo(SERVICE_CENTRE);
    }

    @Test
    void shouldReturnSystemUpdateCasePronouncementJudgeCaseTask() {

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .pronouncementJudge("The Judge")
            .build();

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final ConditionalOrder resultConditionalOrder = resultCaseDetails.getData().getConditionalOrder();

        assertThat(resultConditionalOrder.getPronouncementJudge()).isEqualTo("The Judge");
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

        final var caseData = CaseData.builder()
            .finalOrder(FinalOrder.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseTask caseTask = bulkCaseCaseTaskFactory.getCaseTask(bulkActionCaseData, SYSTEM_PRONOUNCE_CASE);

        final CaseDetails<CaseData, State> resultCaseDetails = caseTask.apply(caseDetails);
        final CaseData resultCaseData = resultCaseDetails.getData();
        final ConditionalOrder resultConditionalOrder = resultCaseData.getConditionalOrder();

        assertThat(resultCaseData.getDueDate()).isEqualTo(expectedDate);
        assertThat(resultConditionalOrder.getOutcomeCase()).isEqualTo(YES);
        assertThat(resultConditionalOrder.getGrantedDate()).isEqualTo(localDate);
        assertThat(resultCaseData.getFinalOrder().getDateFinalOrderEligibleFrom()).isEqualTo(expectedDate);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionIfUnknownEventId() {

        assertThrows(
            IllegalArgumentException.class,
            () -> bulkCaseCaseTaskFactory.getCaseTask(mock(BulkActionCaseData.class), "unknown-event"),
            "Cannot create CaseTask for Event Id: unknown-event");
    }
}