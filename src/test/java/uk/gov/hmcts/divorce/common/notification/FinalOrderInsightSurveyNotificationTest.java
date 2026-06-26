package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderInsightSurveyInvite;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderInsightSurveyNotification.INSIGHT_SURVEY_URL_VALUE;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderInsightSurveyNotification.INSIGHT_SURVEY_URL_VARIABLE;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderInsightSurveyNotification.YOUR_DATA_URL_VALUE;
import static uk.gov.hmcts.divorce.common.notification.FinalOrderInsightSurveyNotification.YOUR_DATA_URL_VARIABLE;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_INSIGHT_SURVEY_LAST_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_INSIGHT_SURVEY_FIRST_REMINDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.FINAL_ORDER_INSIGHT_SURVEY_INVITE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validCaseDataForIssueApplication;

@ExtendWith(MockitoExtension.class)
class FinalOrderInsightSurveyNotificationTest {

    @Mock
    private CommonContent commonContent;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FinalOrderInsightSurveyNotification notification;

    @Test
    void shouldSendInsightSurveyInviteToApplicant1() {
        final CaseData caseData = buildCaseData(
            FinalOrderInsightSurveyInvite.FIRST_NOTIFICATION,
            LocalDateTime.now().minusDays(FinalOrderInsightSurveyInvite.FIRST_NOTIFICATION.getDaysAfterGrantedDate())
        );

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(FINAL_ORDER_INSIGHT_SURVEY_INVITE),
            argThat(allOf(
                hasEntry(INSIGHT_SURVEY_URL_VARIABLE, INSIGHT_SURVEY_URL_VALUE),
                hasEntry(YOUR_DATA_URL_VARIABLE, YOUR_DATA_URL_VALUE)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldSendFirstReminderToApplicant2InWelsh() {
        final CaseData caseData = buildCaseData(
            FinalOrderInsightSurveyInvite.FIRST_REMINDER,
            LocalDateTime.now().minusDays(FinalOrderInsightSurveyInvite.FIRST_REMINDER.getDaysAfterGrantedDate())
        );
        caseData.getApplicant2().setLanguagePreferenceWelsh(YesOrNo.YES);

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1()))
            .thenReturn(new HashMap<>());

        notification.sendToApplicant2(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(caseData.getApplicant2().getEmail()),
            eq(FINAL_ORDER_INSIGHT_SURVEY_FIRST_REMINDER),
            argThat(hasEntry(INSIGHT_SURVEY_URL_VARIABLE, INSIGHT_SURVEY_URL_VALUE)),
            eq(WELSH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());
    }

    @Test
    void shouldSendFinalReminderToApplicant1() {
        final CaseData caseData = buildCaseData(
            FinalOrderInsightSurveyInvite.LAST_REMINDER,
            LocalDateTime.now().minusDays(FinalOrderInsightSurveyInvite.LAST_REMINDER.getDaysAfterGrantedDate())
        );

        when(commonContent.mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2()))
            .thenReturn(new HashMap<>());

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(FINAL_ORDER_INSIGHT_SURVEY_LAST_REMINDER),
            argThat(hasEntry(YOUR_DATA_URL_VARIABLE, YOUR_DATA_URL_VALUE)),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).mainTemplateVars(caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());
    }

    @Test
    void shouldNotSendInsightSurveyBeforeInviteIsDue() {
        final CaseData caseData = buildCaseData(FinalOrderInsightSurveyInvite.FIRST_NOTIFICATION, LocalDateTime.now());

        notification.sendToApplicant1(caseData, TEST_CASE_ID);

        verifyNoInteractions(commonContent, notificationService);
    }

    private CaseData buildCaseData(FinalOrderInsightSurveyInvite invite, LocalDateTime grantedDate) {
        final CaseData caseData = validCaseDataForIssueApplication();
        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);
        caseData.setFinalOrder(FinalOrder.builder()
            .grantedDate(grantedDate)
            .finalOrderInsightSurveyStage(invite.getStage())
            .build());
        return caseData;
    }
}
