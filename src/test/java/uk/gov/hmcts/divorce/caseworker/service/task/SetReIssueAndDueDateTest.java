package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetReIssueAndDueDateTest {

    private static final long DUE_DATE_OFFSET_DAYS = 16L;

    @Mock
    private Clock clock;

    @InjectMocks
    private SetReIssueAndDueDate setReIssueAndDueDate;

    @BeforeEach
    void setPageSize() {
        ReflectionTestUtils.setField(setReIssueAndDueDate, "dueDateOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldSetDueDateAndReIssueDate() {
        setMockClock(clock);
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setReIssueAndDueDate.apply(caseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(DUE_DATE_OFFSET_DAYS);
        final LocalDate expectedReIssueDate = getExpectedLocalDate();

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
        assertThat(result.getData().getApplication().getReissueDate()).isEqualTo(expectedReIssueDate);
    }
}
