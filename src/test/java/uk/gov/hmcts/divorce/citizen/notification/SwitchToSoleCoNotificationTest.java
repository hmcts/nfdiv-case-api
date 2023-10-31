package uk.gov.hmcts.divorce.citizen.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PRONOUNCE_BY_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.PARTNER_SWITCHED_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getMainTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;

@ExtendWith(SpringExtension.class)
class SwitchToSoleCoNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private SwitchToSoleCoNotification notification;

    private static final long CASE_ID = TEST_CASE_ID;

    @Test
    void shouldSendNotificationToApplicant1WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        final Map<String, String> templateVars = getMainTemplateVars();
        when(commonContent.mainTemplateVars(data, CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        notification.sendToApplicant1(data, CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDateTime().plusDays(56).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicant1WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.setConditionalOrder(ConditionalOrder.builder()
            .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder().submittedDate(LocalDateTime.now()).build())
            .build());
        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(templateVars);
        notification.sendToApplicant1(data, CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_APPLIED_FOR_CONDITIONAL_ORDER),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES),
                hasEntry(PRONOUNCE_BY_DATE, getExpectedLocalDateTime().plusDays(56).format(DATE_TIME_FORMATTER))
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendNotificationToApplicant2WithDivorceContent() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(null);

        when(commonContent.mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(PARTNER_SWITCHED_TO_SOLE_CO),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendNotificationToApplicant2WithDivorceContentWelsh() {
        CaseData data = validApplicant2CaseData();
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getMainTemplateVars());

        notification.sendToApplicant2(data, CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(PARTNER_SWITCHED_TO_SOLE_CO),
            argThat(allOf(
                hasEntry(IS_DIVORCE, YES),
                hasEntry(IS_DISSOLUTION, NO)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendNotificationToApplicant2WithDissolutionContent() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplicant2().setEmail(null);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(PARTNER_SWITCHED_TO_SOLE_CO),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldSendNotificationToApplicant2WithDissolutionContentWelsh() {
        CaseData data = validApplicant2CaseData();
        data.setDivorceOrDissolution(DISSOLUTION);
        data.getApplicant2().setEmail(null);
        data.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        final Map<String, String> templateVars = getMainTemplateVars();
        templateVars.putAll(Map.of(IS_DIVORCE, NO, IS_DISSOLUTION, YES));
        when(commonContent.mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(templateVars);

        notification.sendToApplicant2(data, CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(PARTNER_SWITCHED_TO_SOLE_CO),
            argThat(allOf(
                hasEntry(IS_DIVORCE, NO),
                hasEntry(IS_DISSOLUTION, YES)
            )),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(data, CASE_ID, data.getApplicant2(), data.getApplicant1());
    }
}
