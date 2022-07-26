package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetSubmissionAndDueDateTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SetSubmissionAndDueDate setSubmissionAndDueDate;

    @Test
    void shouldSetDueDateAndDateAosSubmittedIfStateIsHolding() {

        setMockClock(clock);

        final LocalDate issueDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(Holding);
        caseDetails.setData(caseData);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(issueDate);

        final CaseDetails<CaseData, State> result = setSubmissionAndDueDate.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getDateAosSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(result.getData().getDueDate()).isEqualTo(issueDate);
    }

    @Test
    void shouldSetDueDateAndDateAosSubmittedIfStateIsWelshTranslationReviewAndWelshPreviousStateIsHolding() {

        setMockClock(clock);

        final LocalDate issueDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.getApplication().setWelshPreviousState(Holding);
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(WelshTranslationReview);
        caseDetails.setData(caseData);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(issueDate);

        final CaseDetails<CaseData, State> result = setSubmissionAndDueDate.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getDateAosSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(result.getData().getDueDate()).isEqualTo(issueDate);
    }

    @Test
    void shouldNotSetDueDateAndDateAosSubmittedIfStateHasNotChanged() {

        setMockClock(clock);

        final LocalDate dueDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.setDueDate(dueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AwaitingConditionalOrder);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmissionAndDueDate.apply(caseDetails);

        assertThat(result.getData().getAcknowledgementOfService().getDateAosSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(result.getData().getDueDate()).isEqualTo(dueDate);
    }
}
