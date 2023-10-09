package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.notification.SwitchToSoleSolicitorTemplateContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_PLUS_14_DAYS;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.INTEND_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.jointCaseDataWithOrderSummary;

@ExtendWith(MockitoExtension.class)
class Applicant1IntendToSwitchToSoleFoNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    SwitchToSoleSolicitorTemplateContent switchToSoleSolicitorTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant1IntendToSwitchToSoleFoNotification applicant1IntendToSwitchToSoleFoNotification;

    @Test
    void shouldSendNotificationToApplicant1() {

        setMockClock(clock);
        final CaseData caseData = caseData();
        final Map<String, String> templateContent = new HashMap<>();
        templateContent.put(DATE_PLUS_14_DAYS, LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER));

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            INTEND_TO_SWITCH_TO_SOLE_FO,
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

        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            INTEND_TO_SWITCH_TO_SOLE_FO,
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

        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
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

        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            TEST_USER_EMAIL,
            PARTNER_INTENDS_TO_SWITCH_TO_SOLE_FO,
            templateContent,
            WELSH,
            TEST_CASE_ID
        );
    }

    @Test
    void shouldSendNotificationToApplicant2Solicitor() {
        final Applicant applicant1 = Applicant.builder()
            .firstName("Julie")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app1 sol")
                    .build()
            )
            .build();
        final Applicant applicant2 = Applicant.builder()
            .firstName("Bob")
            .lastName("Smith")
            .solicitor(
                Solicitor.builder()
                    .name("app2 sol")
                    .build()
            )
            .build();
        final CaseData caseData = CaseData.builder()
            .applicant1(
                applicant1
            )
            .applicant2(
                applicant2
            )
            .finalOrder(FinalOrder.builder().doesApplicant1IntendToSwitchToSole(YES).build())
            .build();

        applicant1IntendToSwitchToSoleFoNotification.sendToApplicant2Solicitor(caseData, TEST_CASE_ID);

        verify(switchToSoleSolicitorTemplateContent).templatevars(caseData,TEST_CASE_ID,applicant2,applicant1);
        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getSolicitor().getEmail()),
            eq(OTHER_APPLICANT_INTENDS_TO_SWITCH_TO_SOLE_FO_SOLICITOR),
            anyMap(),
            eq(caseData.getApplicant2().getLanguagePreference()),
            eq(TEST_CASE_ID)
        );
    }
}
