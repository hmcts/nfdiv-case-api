package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTEND_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.jointCaseDataWithOrderSummary;

@ExtendWith(MockitoExtension.class)
public class Applicant2IntendToSwitchToSoleFoNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant2IntendToSwitchToSoleFoNotification applicant2IntendToSwitchToSoleFoNotification;

    @Test
    void shouldSendNotificationToApplicant1() {

        setMockClock(clock);
        final CaseData caseData = caseData();
        final Map<String, String> templateContent = new HashMap<>();
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        applicant2IntendToSwitchToSoleFoNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendWelshNotificationToApplicant1() {

        setMockClock(clock);
        final CaseData caseData = caseData();
        caseData.getApplicant1().setLanguagePreferenceWelsh(YES);
        final Map<String, String> templateContent = new HashMap<>();
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        applicant2IntendToSwitchToSoleFoNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
            templateContent,
            WELSH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendNotificationToApplicant2() {

        setMockClock(clock);

        final CaseData caseData = jointCaseDataWithOrderSummary();
        final Map<String, String> templateContent = new HashMap<>();
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        applicant2IntendToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            INTEND_TO_SWITCH_TO_SOLE_FO,
            templateContent,
            ENGLISH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendWelshNotificationToApplicant2() {

        setMockClock(clock);

        final CaseData caseData = jointCaseDataWithOrderSummary();
        caseData.getApplicant2().setLanguagePreferenceWelsh(YES);
        final Map<String, String> templateContent = new HashMap<>();
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        applicant2IntendToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            INTEND_TO_SWITCH_TO_SOLE_FO,
            templateContent,
            WELSH,
            TEST_CASE_ID
        );
    }
}
