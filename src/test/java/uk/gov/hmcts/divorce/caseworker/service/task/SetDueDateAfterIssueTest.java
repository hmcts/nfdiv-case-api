package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetDueDateAfterIssueTest {

    private static final long DUE_DATE_OFFSET_DAYS = 16L;
    private static final int HOLDING_PERIOD_IN_DAYS = 141;

    @Mock
    private Clock clock;

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private SetDueDateAfterIssue setDueDateAfterIssue;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(setDueDateAfterIssue, "dueDateOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldSetDueDateIfJointApplication() {
        when(holdingPeriodService.getDueDateFor(any())).thenReturn(LocalDate.now().plusDays(HOLDING_PERIOD_IN_DAYS));
        final var caseData = caseData();
        caseData.getApplication().setIssueDate(LocalDate.now());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDateAfterIssue.apply(caseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(HOLDING_PERIOD_IN_DAYS);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldSetDueDateIfSoleApplicationAndCourtService() {
        setMockClock(clock);
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDateAfterIssue.apply(caseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(DUE_DATE_OFFSET_DAYS);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldSetDueDateIfAndSoleApplicationAndSolicitorService() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplication(
            Application.builder()
                .serviceMethod(SOLICITOR_SERVICE)
                .build()
        );
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDateAfterIssue.apply(caseDetails);

        assertThat(result.getData().getDueDate()).isNull();
    }
}
