package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.FinalOrderNotificationCommonContent.NOW_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class FinalOrderNotificationCommonContentTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock mockClock;

    @InjectMocks
    private FinalOrderNotificationCommonContent finalOrderNotificationCommonContent;

    @Test
    public void shouldReturnJointApplicantVars() {
        CaseData data = validJointApplicant1CaseData();
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        setMockClock(mockClock);

        final Map<String, String> result = finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2(), false);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(NOW_PLUS_14_DAYS, getExpectedLocalDate().plusDays(14).format(DATE_TIME_FORMATTER)),
                entry(IS_REMINDER, "no")
            );
    }

    @Test
    public void shouldReturnJointApplicantVarsForReminder() {
        CaseData data = validJointApplicant1CaseData();
        data.getFinalOrder().setDateFinalOrderSubmitted(LocalDateTime.now());
        when(commonContent.mainTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getMainTemplateVars());

        final Map<String, String> result = finalOrderNotificationCommonContent.jointApplicantTemplateVars(
            data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2(), true);

        assertThat(result)
            .isNotEmpty()
            .contains(
                entry(NOW_PLUS_14_DAYS, getExpectedLocalDate().plusDays(14).format(DATE_TIME_FORMATTER)),
                entry(IS_REMINDER, "yes")
            );
    }
}
