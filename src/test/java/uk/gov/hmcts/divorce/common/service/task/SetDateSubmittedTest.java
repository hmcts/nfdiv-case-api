package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SetDateSubmittedTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private SetDateSubmitted setDateSubmitted;

    @Test
    void shouldSetDateSubmittedIfStateIsSubmitted() {

        setMockClock(clock);
        final LocalDateTime expectedDateSubmitted = getExpectedLocalDateTime();
        final LocalDate expectedDueDate = expectedDateSubmitted.plusDays(14).toLocalDate();

        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> result = setDateSubmitted.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Submitted);
        final CaseData resultData = result.getData();
        assertThat(resultData.getApplication().getDateSubmitted()).isEqualTo(expectedDateSubmitted);
        assertThat(resultData.getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldSetDateSubmittedIfStateIsAwaitingDocuments() {

        setMockClock(clock);
        final LocalDateTime expectedDateSubmitted = getExpectedLocalDateTime();
        final LocalDate expectedDueDate = expectedDateSubmitted.plusDays(14).toLocalDate();

        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        final CaseDetails<CaseData, State> result = setDateSubmitted.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingDocuments);
        final CaseData resultData = result.getData();
        assertThat(resultData.getApplication().getDateSubmitted()).isEqualTo(expectedDateSubmitted);
        assertThat(resultData.getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldNotSetDateSubmittedIfStateIsNotSubmitted() {

        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        final CaseDetails<CaseData, State> result = setDateSubmitted.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingHWFDecision);
        final CaseData resultData = result.getData();
        assertThat(resultData.getApplication().getDateSubmitted()).isNull();
        assertThat(resultData.getDueDate()).isNull();
    }
}