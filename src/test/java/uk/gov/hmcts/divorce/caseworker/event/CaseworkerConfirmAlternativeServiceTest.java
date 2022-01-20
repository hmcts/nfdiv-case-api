package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerConfirmAlternativeService.CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerConfirmAlternativeServiceTest {

    private static final long DUE_DATE_OFFSET_DAYS = 16L;

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerConfirmAlternativeService caseworkerConfirmAlternativeService;

    @BeforeEach
    void setDueDateOffsetDays() {
        setField(caseworkerConfirmAlternativeService, "dueDateOffsetDays", DUE_DATE_OFFSET_DAYS);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerConfirmAlternativeService.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE);
    }

    @Test
    void shouldSetDueDateOnAboutToSubmit() {

        setMockClock(clock);
        final var caseData = caseData();

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var response = caseworkerConfirmAlternativeService.aboutToSubmit(caseDetails, beforeCaseDetails);

        final LocalDate expectedDueDate = getExpectedLocalDate().plusDays(DUE_DATE_OFFSET_DAYS);

        assertThat(response.getData().getDueDate()).isEqualTo(expectedDueDate);
    }
}
