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
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
class SetDueDateTest {

    private static final long DUE_DATE_OFFSET_DAYS = 14L;
    private static final long HOLDING_PERIOD_IN_WEEKS = 20L;
    private static final long SOLE_DUE_DATE_OFFSET_DAYS = 14L;

    @Mock
    private Clock clock;

    @InjectMocks
    private SetDueDate setDueDate;

    @BeforeEach
    void setPageSize() {
        ReflectionTestUtils.setField(setDueDate, "dueDateOffsetDays", DUE_DATE_OFFSET_DAYS);
        ReflectionTestUtils.setField(setDueDate, "holdingPeriodInWeeks", HOLDING_PERIOD_IN_WEEKS);
        ReflectionTestUtils.setField(setDueDate, "soleDueDateOffsetDays", SOLE_DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldNotSetDueDateIfSolicitorService() {
        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolServiceMethod(SOLICITOR_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDate.apply(caseDetails);

        assertThat(result.getData().getDueDate()).isNull();

        verifyNoInteractions(clock);
    }

    @Test
    void shouldSetDueDateIfNotPersonalService() {
        setMockClock(clock);
        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        caseData.setApplicant2(respondent());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDate.apply(caseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(DUE_DATE_OFFSET_DAYS);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldSetDueDateIfNotSolicitorApplicationAndJointApplication() {
        setMockClock(clock);
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDate.apply(caseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusWeeks(HOLDING_PERIOD_IN_WEEKS).plusDays(1);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldSetDueDateIfNotSolicitorApplicationAndSoleApplication() {
        setMockClock(clock);
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setDueDate.apply(caseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(14);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }
}
